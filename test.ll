; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @f(i32 %0, i32 %1) {
fEntry:
  %i = alloca i32, align 4
  store i32 %0, i32* %i, align 4
  %j = alloca i32, align 4
  store i32 %1, i32* %j, align 4
  %i1 = load i32, i32* %i, align 4
  %j2 = load i32, i32* %j, align 4
  %2 = mul i32 %i1, %j2
  store i32 %2, i32* %j, align 4
  %j3 = load i32, i32* %j, align 4
  ret i32 %j3
}

define i32 @main() {
mainEntry:
  %array = alloca [3 x i32], align 4
  %pointer = getelementptr [3 x i32], [3 x i32]* %array, i32 0, i32 0
  store i32 0, i32* %pointer, align 4
  %pointer1 = getelementptr [3 x i32], [3 x i32]* %array, i32 0, i32 1
  store i32 0, i32* %pointer1, align 4
  %pointer2 = getelementptr [3 x i32], [3 x i32]* %array, i32 0, i32 2
  store i32 0, i32* %pointer2, align 4
  %a = alloca i32, align 4
  store i32 12, i32* %a, align 4
  %d = alloca i32, align 4
  store i32 3, i32* %d, align 4
  %b = alloca i32, align 4
  store i32 4, i32* %b, align 4
  %c = alloca i32, align 4
  %a3 = load i32, i32* %a, align 4
  %b4 = load i32, i32* %b, align 4
  %0 = add i32 %a3, %b4
  store i32 %0, i32* %c, align 4
  %pointer5 = getelementptr [3 x i32], [3 x i32]* %array, i32 0, i32 2
  %array6 = load i32, i32* %pointer5, align 4
  ret i32 %array6
}
