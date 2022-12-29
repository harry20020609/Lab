; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 2, i32* %a, align 4
  %array = alloca [5 x i32], align 4
  %pointer = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 0
  store i32 0, i32* %pointer, align 4
  %pointer1 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 1
  store i32 0, i32* %pointer1, align 4
  %pointer2 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 2
  store i32 0, i32* %pointer2, align 4
  %pointer3 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 3
  store i32 0, i32* %pointer3, align 4
  %pointer4 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 4
  store i32 0, i32* %pointer4, align 4
  %a5 = load i32, i32* %a, align 4
  %pointer6 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 %a5
  %array7 = load i32, i32* %pointer6, align 4
  ret i32 %array7
}
