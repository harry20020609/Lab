; ModuleID = 'moudle'
source_filename = "moudle"

@a = global i32 2
@b = global i32 0

define i32 @main() {
mainEntry:
  store i32 3, i32* @b, align 4
  %a = load i32, i32* @a, align 4
  %b = load i32, i32* @b, align 4
  %plus = add i32 %a, %b
  ret i32 %plus
}
