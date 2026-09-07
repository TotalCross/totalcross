// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <algorithm>
#include <chrono>
#include <cmath>
#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <limits>
#include <new>
#include <stdexcept>
#include <string>
#include <vector>

#if defined(_MSC_VER)
#include <intrin.h>
#else
#include <sys/utsname.h>
#endif

#if defined(_MSC_VER)
#define NATIVE_SWAP_NOINLINE __declspec(noinline)
#else
#define NATIVE_SWAP_NOINLINE __attribute__((noinline))
#endif

namespace {

using Clock = std::chrono::steady_clock;
using SwapFunction = uint32_t (*)(uint32_t) noexcept;

volatile uint64_t checksum_sink = 0;

NATIVE_SWAP_NOINLINE uint32_t native_swap32(uint32_t value) noexcept {
#if defined(_MSC_VER)
    return _byteswap_ulong(value);
#elif defined(__clang__) || defined(__GNUC__)
    return __builtin_bswap32(value);
#else
#error "native_swap_benchmark requires GCC, Clang, or MSVC"
#endif
}

NATIVE_SWAP_NOINLINE uint32_t portable_swap32(uint32_t value) noexcept {
    return (((value >> 24) & 0xFFu)) |
           ((((value >> 16) & 0xFFu) << 8) |
            (((value >> 8) & 0xFFu) << 16) | ((value & 0xFFu) << 24));
}

size_t parse_count(const char* text, const char* name, bool allow_zero = false) {
    char* end = nullptr;
    const unsigned long long parsed = std::strtoull(text, &end, 10);
    if (end == text || *end != '\0' || (!allow_zero && parsed == 0) ||
        parsed > std::numeric_limits<size_t>::max()) {
        throw std::invalid_argument(std::string("invalid ") + name);
    }
    return static_cast<size_t>(parsed);
}

uint64_t checksum(const std::vector<uint32_t>& values) noexcept {
    uint64_t result = 1469598103934665603ULL;
    for (const uint32_t value : values) {
        result ^= value;
        result *= 1099511628211ULL;
    }
    return result;
}

void swap_buffer(const std::vector<uint32_t>& source,
                 std::vector<uint32_t>& destination,
                 SwapFunction swap_function) noexcept {
    for (size_t index = 0; index < source.size(); ++index) {
        destination[index] = swap_function(source[index]);
    }
}

uint64_t measure(const std::vector<uint32_t>& source,
                 std::vector<uint32_t>& destination,
                 SwapFunction swap_function,
                 uint64_t& result_checksum) noexcept {
    std::fill(destination.begin(), destination.end(), 0xA5A5A5A5u);
    const auto start = Clock::now();
    swap_buffer(source, destination, swap_function);
    const auto finish = Clock::now();
    result_checksum = checksum(destination);
    checksum_sink ^= result_checksum;
    return static_cast<uint64_t>(
        std::chrono::duration_cast<std::chrono::nanoseconds>(finish - start).count());
}

std::string compiler_version() {
#if defined(_MSC_VER)
    return "MSVC " + std::to_string(_MSC_VER) + "." +
           std::to_string(_MSC_FULL_VER % 100000);
#elif defined(__clang__)
    return std::string("Clang ") + __VERSION__;
#elif defined(__GNUC__)
    return std::string("GCC ") + __VERSION__;
#else
    return "unknown";
#endif
}

std::string operating_system() {
#if defined(_WIN32)
    return "Windows";
#elif defined(__APPLE__)
    return "macOS";
#elif defined(__linux__)
    return "Linux";
#else
    return "unknown";
#endif
}

std::string architecture() {
#if defined(_MSC_VER)
#if defined(_M_ARM64)
    return "ARM64";
#elif defined(_M_X64)
    return "x86-64";
#elif defined(_M_IX86)
    return "x86";
#else
    return "unknown";
#endif
#else
    struct utsname system_info {};
    if (uname(&system_info) == 0) {
        return system_info.machine;
    }
#if defined(__aarch64__)
    return "aarch64";
#elif defined(__x86_64__)
    return "x86_64";
#else
    return "unknown";
#endif
#endif
}

std::string json_escape(const std::string& value) {
    std::string escaped;
    escaped.reserve(value.size());
    for (const char character : value) {
        if (character == '\\' || character == '"') {
            escaped.push_back('\\');
        }
        escaped.push_back(character);
    }
    return escaped;
}

uint64_t percentile(std::vector<uint64_t> values, double percentile_value) {
    std::sort(values.begin(), values.end());
    const size_t index = static_cast<size_t>(
        std::ceil(percentile_value * static_cast<double>(values.size())) - 1.0);
    return values[std::min(index, values.size() - 1)];
}

void write_summary(const std::string& path,
                   size_t width,
                   size_t height,
                   size_t warmups,
                   size_t samples,
                   const std::vector<uint64_t>& native_times,
                   const std::vector<uint64_t>& portable_times,
                   bool checksums_agree) {
    const uint64_t native_median = percentile(native_times, 0.50);
    const uint64_t portable_median = percentile(portable_times, 0.50);
    const uint64_t native_p95 = percentile(native_times, 0.95);
    const uint64_t portable_p95 = percentile(portable_times, 0.95);
    const double ratio = static_cast<double>(native_median) /
                         static_cast<double>(portable_median);

    std::ofstream output(path);
    if (!output) {
        throw std::runtime_error("cannot open summary output");
    }
    output << "{\n"
           << "  \"width\": " << width << ",\n"
           << "  \"height\": " << height << ",\n"
           << "  \"pixel_count\": " << (width * height) << ",\n"
           << "  \"compiler\": \"" << json_escape(compiler_version()) << "\",\n"
           << "  \"os\": \"" << operating_system() << "\",\n"
           << "  \"architecture\": \"" << architecture() << "\",\n"
           << "  \"warmups\": " << warmups << ",\n"
           << "  \"samples\": " << samples << ",\n"
           << "  \"native_median_ns\": " << native_median << ",\n"
           << "  \"native_p95_ns\": " << native_p95 << ",\n"
           << "  \"portable_median_ns\": " << portable_median << ",\n"
           << "  \"portable_p95_ns\": " << portable_p95 << ",\n"
           << "  \"native_portable_ratio\": " << ratio << ",\n"
           << "  \"checksum_agreement\": " << (checksums_agree ? "true" : "false")
           << "\n}\n";
    if (!output) {
        throw std::runtime_error("cannot write summary output");
    }
}

}  // namespace

int main(int argc, char** argv) {
    if (argc != 7) {
        return 2;
    }

    try {
        const size_t width = parse_count(argv[1], "width");
        const size_t height = parse_count(argv[2], "height");
        const size_t warmups = parse_count(argv[3], "warmups", true);
        const size_t samples = parse_count(argv[4], "samples");
        if (width > std::numeric_limits<size_t>::max() / height ||
            width * height > std::numeric_limits<size_t>::max() / sizeof(uint32_t)) {
            throw std::invalid_argument("image is too large");
        }
        const size_t pixel_count = width * height;

        std::vector<uint32_t> source(pixel_count);
        std::vector<uint32_t> destination(pixel_count);
        uint32_t value = 0x12345678u;
        for (uint32_t& pixel : source) {
            value = value * 1664525u + 1013904223u;
            pixel = value ^ static_cast<uint32_t>(&pixel - source.data());
        }

        for (size_t sample = 0; sample < warmups; ++sample) {
            uint64_t native_checksum = 0;
            uint64_t portable_checksum = 0;
            if ((sample & 1u) == 0) {
                measure(source, destination, native_swap32, native_checksum);
                measure(source, destination, portable_swap32, portable_checksum);
            } else {
                measure(source, destination, portable_swap32, portable_checksum);
                measure(source, destination, native_swap32, native_checksum);
            }
            if (native_checksum != portable_checksum) {
                throw std::runtime_error("checksum mismatch during warmup");
            }
        }

        std::ofstream raw(argv[5]);
        if (!raw) {
            throw std::runtime_error("cannot open raw output");
        }
        raw << "sample,order,native_ns,portable_ns,native_checksum,portable_checksum\n";

        std::vector<uint64_t> native_times;
        std::vector<uint64_t> portable_times;
        native_times.reserve(samples);
        portable_times.reserve(samples);
        bool checksums_agree = true;
        for (size_t sample = 0; sample < samples; ++sample) {
            uint64_t native_checksum = 0;
            uint64_t portable_checksum = 0;
            uint64_t native_time = 0;
            uint64_t portable_time = 0;
            if ((sample & 1u) == 0) {
                native_time = measure(source, destination, native_swap32, native_checksum);
                portable_time = measure(source, destination, portable_swap32, portable_checksum);
            } else {
                portable_time = measure(source, destination, portable_swap32, portable_checksum);
                native_time = measure(source, destination, native_swap32, native_checksum);
            }
            checksums_agree = checksums_agree && native_checksum == portable_checksum;
            native_times.push_back(native_time);
            portable_times.push_back(portable_time);
            raw << sample << ',' << (((sample & 1u) == 0) ? "native-portable" : "portable-native")
                << ',' << native_time << ',' << portable_time << ','
                << native_checksum << ',' << portable_checksum << '\n';
        }
        if (!raw) {
            throw std::runtime_error("cannot write raw output");
        }
        raw.close();
        if (!checksums_agree) {
            throw std::runtime_error("checksum mismatch");
        }
        write_summary(argv[6], width, height, warmups, samples, native_times,
                      portable_times, checksums_agree);
        return 0;
    } catch (const std::exception&) {
        return 1;
    }
}
