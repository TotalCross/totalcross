#!/usr/bin/env ruby

require 'digest'

DEPENDENCY_LIBRARIES = %w[
  libmbedcrypto.a
  libmbedtls.a
  libmbedx509.a
  libpng16.a
  libSQLite3.a
  libz.a
].freeze

def stable_id(seed, content)
  salt = 0

  loop do
    candidate = Digest::MD5.hexdigest("#{seed}:#{salt}").upcase[0, 24]
    return candidate unless content.include?(candidate)

    salt += 1
  end
end

def find_unique(content, regex, description)
  match = content.match(regex)
  raise "Unable to locate #{description}" unless match

  match
end

def insert_before(content, marker, text)
  index = content.index(marker)
  raise "Unable to locate marker: #{marker}" unless index

  content.dup.insert(index, text)
end

project_path = ARGV[0]
if project_path.nil? || project_path.empty?
  abort "usage: #{$PROGRAM_NAME} /path/to/TCVM.xcodeproj/project.pbxproj"
end

content = File.read(project_path)
changed = false

tcvm_target_match = find_unique(
  content,
  /([0-9A-F]{24}) \/\* tcvm \*\/ = \{\n\t\t\tisa = PBXNativeTarget;.*?\n\t\t\tbuildPhases = \(\n(?<build_phases>.*?)\n\t\t\t\);\n.*?\n\t\t\tproductReference = [0-9A-F]{24} \/\* libtcvm\.a \*\/;\n\t\t\tproductType = "com\.apple\.product-type\.library\.static";\n\t\t\};/m,
  'tcvm native target'
)

framework_phase_id = tcvm_target_match[:build_phases][/^\t\t\t\t([0-9A-F]{24}) \/\* Frameworks \*\/,$/, 1]
raise 'Unable to locate tcvm Frameworks build phase' unless framework_phase_id

framework_phase_match = find_unique(
  content,
  /(#{framework_phase_id} \/\* Frameworks \*\/ = \{\n\t\t\tisa = PBXFrameworksBuildPhase;\n\t\t\tbuildActionMask = 2147483647;\n\t\t\tfiles = \(\n)(?<files>.*?)(\n\t\t\t\);\n\t\t\trunOnlyForDeploymentPostprocessing = 0;\n\t\t\};)/m,
  'tcvm Frameworks build phase block'
)

framework_phase_entries = framework_phase_match[:files]
normalized_framework_phase_entries = framework_phase_entries.gsub(/[ \t]+/, '')
new_build_file_lines = []
missing_framework_entries = []

DEPENDENCY_LIBRARIES.each do |library_name|
  file_ref_match = find_unique(
    content,
    /^(\s*)([0-9A-F]{24}) \/\* #{Regexp.escape(library_name)} \*\/ = \{isa = PBXFileReference;.*?path = #{Regexp.escape(library_name)};.*?\};$/m,
    "#{library_name} file reference"
  )
  file_ref_id = file_ref_match[2]

  build_file_pattern = /^\s*([0-9A-F]{24}) \/\* #{Regexp.escape(library_name)} in Frameworks \*\/ = \{isa = PBXBuildFile;.*\};$/
  build_file_match = content.match(build_file_pattern)

  build_file_id =
    if build_file_match
      build_file_match[1]
    else
      generated_id = stable_id("pbxbuildfile:#{library_name}", content + new_build_file_lines.join)
      new_build_file_lines << "\t\t#{generated_id} /* #{library_name} in Frameworks */ = {isa = PBXBuildFile; fileRef = #{file_ref_id} /* #{library_name} */; };\n"
      generated_id
    end

  next if normalized_framework_phase_entries.include?("/*#{library_name}inFrameworks*/,")

  missing_framework_entries << "\t\t\t\t#{build_file_id} /* #{library_name} in Frameworks */,\n"
end

framework_phase_entries_without_pods = framework_phase_entries.gsub(
  /^\s*[0-9A-F]{24} \/\* libPods-tcvm\.a in Frameworks \*\/,\n?/,
  ''
)

if framework_phase_entries_without_pods != framework_phase_entries
  framework_phase_entries = framework_phase_entries_without_pods
  changed = true
end

unless new_build_file_lines.empty?
  content = insert_before(content, "/* End PBXBuildFile section */\n", new_build_file_lines.join)
  changed = true
end

unless missing_framework_entries.empty?
  insertion = missing_framework_entries.join
  updated_entries =
    if framework_phase_entries.include?('libPods-tcvm.a in Frameworks')
      framework_phase_entries.sub(
        /(\t\t\t\t[0-9A-F]{24} \/\* libPods-tcvm\.a in Frameworks \*\/,\n?)/,
        insertion + "\\1"
      )
    else
      suffix = framework_phase_entries.end_with?("\n") || framework_phase_entries.empty? ? '' : "\n"
      framework_phase_entries + suffix + insertion
    end

  updated_framework_phase = framework_phase_match[0].sub(
    framework_phase_entries,
    updated_entries
  )
  content.sub!(framework_phase_match[0], updated_framework_phase)
  changed = true
end

if changed
  File.write(project_path, content)
  puts "Updated #{project_path}"
else
  puts "No changes needed in #{project_path}"
end
