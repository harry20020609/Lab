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
  %a = alloca [3 x i32], align 4
  %pointer = getelementptr [3 x i32], [3 x i32]* %a, i32 0
  %pointer1 = bitcast [3 x i32]* %pointer to i32*
  store i32 1, i32* %pointer1, align 4
  %pointer2 = getelementptr [3 x i32], [3 x i32]* %a, i32 1
  %pointer3 = bitcast [3 x i32]* %pointer2 to i32*
  store i32 2, i32* %pointer3, align 4
  %pointer4 = getelementptr [3 x i32], [3 x i32]* %a, i32 2
  %pointer5 = bitcast [3 x i32]* %pointer4 to i32*
  store i32 3, i32* %pointer5, align 4
  %b = alloca i32, align 4
  store i32 4, i32* %b, align 4
  %pointer6 = getelementptr [3 x i32], [3 x i32]* %a, i32 1
  %pointer7 = bitcast [3 x i32]* %pointer6 to i32*
  %a8 = load i32, i32* %pointer7, align 4
  ret i32 %a8
}
