; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  %c = alloca i32, align 4
  store i32 3, i32* %c, align 4
  %b = alloca i32, align 4
  store i32 2, i32* %b, align 4
  %a1 = load i32, i32* %a, align 4
  %c2 = load i32, i32* %c, align 4
  %0 = mul i32 %a1, %c2
  store i32 %0, i32* %b, align 4
  ret i32 1
}
