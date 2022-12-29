; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @f(i32 %0) {
fEntry:
  %i = alloca i32, align 4
  store i32 %0, i32* %i, align 4
  %i1 = load i32, i32* %i, align 4
  %i2 = load i32, i32* %i, align 4
  %1 = mul i32 %i1, %i2
  ret i32 %1
}

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  %b = alloca i32, align 4
  store i32 9, i32* %b, align 4
  %array = alloca [5 x i32], align 4
  %pointer = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 0
  store i32 0, i32* %pointer, align 4
  %pointer1 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 1
  store i32 1, i32* %pointer1, align 4
  %pointer2 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 2
  store i32 2, i32* %pointer2, align 4
  %pointer3 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 3
  store i32 0, i32* %pointer3, align 4
  %pointer4 = getelementptr [5 x i32], [5 x i32]* %array, i32 0, i32 4
  store i32 0, i32* %pointer4, align 4
  %c = alloca i32, align 4
  %a5 = load i32, i32* %a, align 4
  %returnValue = call i32 @f(i32 %a5)
  %b6 = load i32, i32* %b, align 4
  %0 = mul i32 %returnValue, %b6
  store i32 %0, i32* %c, align 4
  %c7 = load i32, i32* %c, align 4
  ret i32 %c7
}
