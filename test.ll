; ModuleID = 'moudle'
source_filename = "moudle"

define void @f() {
fEntry:
  ret void
}

define i32 @main() {
mainEntry:
  call void @f()
  ret i32 1
}
