; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @f(i32* %0) {
fEntry:
  %a = alloca i32*, align 8
  store i32* %0, i32** %a, align 8
  %a1 = load i32*, i32** %a, align 8
  %pointer = getelementptr i32, i32* %a1, i32 0
  %a2 = load i32, i32* %pointer, align 4
  ret i32 %a2
}

define i32 @main() {
mainEntry:
  %b = alloca [3 x i32], align 4
  %pointer = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 0
  store i32 1, i32* %pointer, align 4
  %pointer1 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 1
  store i32 2, i32* %pointer1, align 4
  %pointer2 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 2
  store i32 3, i32* %pointer2, align 4
  %pointer3 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 1
  %b4 = load i32, i32* %pointer3, align 4
  ret i32 %b4
}
