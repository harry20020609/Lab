; ModuleID = 'moudle'
source_filename = "moudle"

define void @f(i32 %0) {
fEntry:
  %i = alloca i32, align 4
  store i32 %0, i32* %i, align 4
  store i32 3, i32* %i, align 4
  ret void
}

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  br label %mainBlock
  %b = alloca i32, align 4
  store i32 5, i32* %b, align 4
  %a1 = load i32, i32* %a, align 4
  ret i32 %a1

mainBlock:                                        ; preds = %mainEntry
  br label %mainEntry
}
