// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <algorithm>
#include <array>
#include <chrono>
#include <cmath>
#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <limits>
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
constexpr size_t kVariantCount = 4;

enum class Variant : size_t {
    Portable = 0,
    Forced,
    ForcedImpl,
    Builtin,
};

constexpr std::array<const char*, kVariantCount> kVariantNames = {
    "SWAP32_PORTABLE",
    "SWAP32_FORCED",
    "swap32_forced_impl",
    "builtinSwap32",
};

volatile uint64_t checksum_sink = 0;

NATIVE_SWAP_NOINLINE void SWAP32_PORTABLE(const uint32_t* source,
                                          uint32_t* destination,
                                          size_t pixel_count) noexcept {
    for (size_t index = 0; index < pixel_count; ++index) {
        destination[index] = (((source[index] >> 24) & 0xFF)) |
                             ((((source[index] >> 16) & 0xFF) << 8) |
                              (((source[index] >> 8) & 0xFF) << 16) |
                              ((source[index] & 0xFF) << 24));
    }
}

NATIVE_SWAP_NOINLINE void SWAP32_FORCED(const uint32_t* source,
                                        uint32_t* destination,
                                        size_t pixel_count) noexcept {
    for (size_t index = 0; index < pixel_count; ++index) {
        destination[index] = (((unsigned long) source[index] << 24) & 0xFF000000) |
                             ((((unsigned long) source[index] << 8) & 0x00FF0000) |
                              (((unsigned long) source[index] >> 8) & 0x0000FF00) |
                              (((unsigned long) source[index] >> 24) & 0x000000FF));
    }
}

NATIVE_SWAP_NOINLINE void swap32_forced_impl(const uint32_t* source,
                                             uint32_t* destination,
                                             size_t pixel_count) noexcept {
    for (size_t index = 0; index < pixel_count; ++index) {
        const uint32_t value = source[index];
        destination[index] = ((value >> 24) & 0x000000FFu) |
                             ((value >> 8) & 0x0000FF00u) |
                             ((value << 8) & 0x00FF0000u) |
                             ((value << 24) & 0xFF000000u);
    }
}

NATIVE_SWAP_NOINLINE void builtinSwap32(const uint32_t* source,
                                        uint32_t* destination,
                                        size_t pixel_count) noexcept {
#if defined(_MSC_VER)
    for (size_t index = 0; index < pixel_count; ++index) {
        destination[index] = _byteswap_ulong(source[index]);
    }
#elif defined(__clang__) || defined(__GNUC__)
    for (size_t index = 0; index < pixel_count; ++index) {
        destination[index] = __builtin_bswap32(source[index]);
    }
#else
#error "native_swap_benchmark requires GCC, Clang, or MSVC"
#endif
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

void run_variant(Variant variant,
                 const std::vector<uint32_t>& source,
                 std::vector<uint32_t>& destination) noexcept {
    switch (variant) {
        case Variant::Portable:
            SWAP32_PORTABLE(source.data(), destination.data(), source.size());
            break;
        case Variant::Forced:
            SWAP32_FORCED(source.data(), destination.data(), source.size());
            break;
        case Variant::ForcedImpl:
            swap32_forced_impl(source.data(), destination.data(), source.size());
            break;
        case Variant::Builtin:
            builtinSwap32(source.data(), destination.data(), source.size());
            break;
    }
}

uint64_t measure_variant(Variant variant,
                         const std::vector<uint32_t>& source,
                         std::vector<uint32_t>& destination,
                         uint64_t& result_checksum) noexcept {
    std::fill(destination.begin(), destination.end(), 0xA5A5A5A5u);
    const auto start = Clock::now();
    run_variant(variant, source, destination);
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

std::string order_string(size_t sample) {
    std::string order;
    for (size_t slot = 0; slot < kVariantCount; ++slot) {
        if (slot != 0) {
            order += ">";
        }
        const size_t variant_index = (sample + slot) % kVariantCount;
        order += kVariantNames[variant_index];
    }
    return order;
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
                   const std::array<std::vector<uint64_t>, kVariantCount>& times,
                   bool checksums_agree) {
    std::array<uint64_t, kVariantCount> medians {};
    std::array<uint64_t, kVariantCount> p95s {};
    for (size_t variant = 0; variant < kVariantCount; ++variant) {
        medians[variant] = percentile(times[variant], 0.50);
        p95s[variant] = percentile(times[variant], 0.95);
    }

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
           << "  \"samples\": " << samples << ",\n";
    for (size_t variant = 0; variant < kVariantCount; ++variant) {
        output << "  \"" << kVariantNames[variant] << "_median_ns\": "
               << medians[variant] << ",\n"
               << "  \"" << kVariantNames[variant] << "_p95_ns\": "
               << p95s[variant] << ",\n";
    }
    for (size_t variant = 0; variant < kVariantCount; ++variant) {
        const double ratio = static_cast<double>(medians[variant]) /
                             static_cast<double>(medians[0]);
        output << "  \"" << kVariantNames[variant]
               << "_to_portable_ratio\": " << ratio << ",\n";
    }
    output << "  \"checksum_agreement\": "
           << (checksums_agree ? "true" : "false") << "\n}\n";
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
            std::array<uint64_t, kVariantCount> warmup_checksums {};
            for (size_t slot = 0; slot < kVariantCount; ++slot) {
                const Variant variant = static_cast<Variant>((sample + slot) % kVariantCount);
                measure_variant(variant, source, destination,
                                warmup_checksums[static_cast<size_t>(variant)]);
            }
            for (size_t variant = 1; variant < kVariantCount; ++variant) {
                if (warmup_checksums[variant] != warmup_checksums[0]) {
                    throw std::runtime_error("checksum mismatch during warmup");
                }
            }
        }

        std::ofstream raw(argv[5]);
        if (!raw) {
            throw std::runtime_error("cannot open raw output");
        }
        raw << "sample,order";
        for (const char* name : kVariantNames) {
            raw << "," << name << "_ns";
        }
        for (const char* name : kVariantNames) {
            raw << "," << name << "_checksum";
        }
        raw << "\n";

        std::array<std::vector<uint64_t>, kVariantCount> times;
        for (auto& variant_times : times) {
            variant_times.reserve(samples);
        }
        bool checksums_agree = true;
        for (size_t sample = 0; sample < samples; ++sample) {
            std::array<uint64_t, kVariantCount> sample_times {};
            std::array<uint64_t, kVariantCount> sample_checksums {};
            for (size_t slot = 0; slot < kVariantCount; ++slot) {
                const Variant variant = static_cast<Variant>((sample + slot) % kVariantCount);
                const size_t variant_index = static_cast<size_t>(variant);
                sample_times[variant_index] = measure_variant(
                    variant, source, destination, sample_checksums[variant_index]);
                times[variant_index].push_back(sample_times[variant_index]);
            }
            for (size_t variant = 1; variant < kVariantCount; ++variant) {
                checksums_agree = checksums_agree &&
                                  sample_checksums[variant] == sample_checksums[0];
            }
            raw << sample << ',' << order_string(sample);
            for (const uint64_t elapsed : sample_times) {
                raw << ',' << elapsed;
            }
            for (const uint64_t result_checksum : sample_checksums) {
                raw << ',' << result_checksum;
            }
            raw << '\n';
        }
        if (!raw) {
            throw std::runtime_error("cannot write raw output");
        }
        raw.close();
        if (!checksums_agree) {
            throw std::runtime_error("checksum mismatch");
        }
        write_summary(argv[6], width, height, warmups, samples, times,
                      checksums_agree);
        return 0;
    } catch (const std::exception&) {
        return 1;
    }
}
