; ModuleID = 'moudle'
source_filename = "moudle"

define void @f(i32 %0) {
fEntry:
  %i = alloca i32, align 4
  store i32 %0, i32* %i, align 4
  ret void
}

define i32 @main() {
mainEntry:
  call void @f(i32 1)
  ret i32 1
}
