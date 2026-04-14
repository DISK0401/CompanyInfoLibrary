import re

path = '../../batch/src/main/java/com/companylib/batch/infrastructure/gbizinfo/dto/HojinInfo.java'
content = open(path, 'r', encoding='utf-8').read()

# @Data の直後に public static class が来るが @JsonIgnoreProperties がまだない箇所に追加
fixed = re.sub(
    r'(    @Data\n)(?!    @JsonIgnoreProperties)(    public static class )',
    r'\1    @JsonIgnoreProperties(ignoreUnknown = true)\n\2',
    content
)

open(path, 'w', encoding='utf-8').write(fixed)
count = fixed.count('@JsonIgnoreProperties(ignoreUnknown = true)')
print(f'Applied @JsonIgnoreProperties to {count} classes (including top-level)')
