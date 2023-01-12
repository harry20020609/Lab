; ModuleID = 'moudle'
source_filename = "moudle"

@a = global [10 x i32] [i32 5, i32 4, i32 3, i32 2, i32 1, i32 8, i32 6, i32 2, i32 4, i32 7]

define i32 @main() {
mainEntry:
  %i = alloca i32, align 4
  store i32 0, i32* %i, align 4
  %j = alloca i32, align 4
  store i32 0, i32* %j, align 4
  %min = alloca i32, align 4
  store i32 0, i32* %min, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry30, %mainEntry
  %i1 = load i32, i32* %i, align 4
  %cmp = icmp slt i32 %i1, 10
  %cmp2 = zext i1 %cmp to i32
  %cmp3 = icmp ne i32 %cmp2, 0
  br i1 %cmp3, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %i4 = load i32, i32* %i, align 4
  store i32 %i4, i32* %j, align 4
  %i5 = load i32, i32* %i, align 4
  store i32 %i5, i32* %min, align 4
  br label %whileCondition6

entry:                                            ; preds = %whileCondition
  %a44 = load i32, i32* getelementptr inbounds ([10 x i32], [10 x i32]* @a, i32 0, i32 9), align 4
  ret i32 %a44

whileCondition6:                                  ; preds = %entry20, %whileBody
  %j7 = load i32, i32* %j, align 4
  %cmp8 = icmp slt i32 %j7, 10
  %cmp9 = zext i1 %cmp8 to i32
  %cmp10 = icmp ne i32 %cmp9, 0
  br i1 %cmp10, label %whileBody11, label %entry12

whileBody11:                                      ; preds = %whileCondition6
  %j13 = load i32, i32* %j, align 4
  %pointer = getelementptr [10 x i32], [10 x i32]* @a, i32 0, i32 %j13
  %a = load i32, i32* %pointer, align 4
  %min14 = load i32, i32* %min, align 4
  %pointer15 = getelementptr [10 x i32], [10 x i32]* @a, i32 0, i32 %min14
  %a16 = load i32, i32* %pointer15, align 4
  %cmp17 = icmp slt i32 %a, %a16
  %cmp18 = zext i1 %cmp17 to i32
  %cmp19 = icmp ne i32 %cmp18, 0
  br i1 %cmp19, label %if_true, label %if_false

entry12:                                          ; preds = %whileCondition6
  %min23 = load i32, i32* %min, align 4
  %i24 = load i32, i32* %i, align 4
  %cmp25 = icmp ne i32 %min23, %i24
  %cmp26 = zext i1 %cmp25 to i32
  %cmp27 = icmp ne i32 %cmp26, 0
  br i1 %cmp27, label %if_true28, label %if_false29

if_true:                                          ; preds = %whileBody11
  %j21 = load i32, i32* %j, align 4
  store i32 %j21, i32* %min, align 4
  br label %entry20

if_false:                                         ; preds = %whileBody11
  br label %entry20

entry20:                                          ; preds = %if_false, %if_true
  %j22 = load i32, i32* %j, align 4
  %plus = add i32 %j22, 1
  store i32 %plus, i32* %j, align 4
  br label %whileCondition6

if_true28:                                        ; preds = %entry12
  %temp = alloca i32, align 4
  %min31 = load i32, i32* %min, align 4
  %pointer32 = getelementptr [10 x i32], [10 x i32]* @a, i32 0, i32 %min31
  %a33 = load i32, i32* %pointer32, align 4
  store i32 %a33, i32* %temp, align 4
  %min34 = load i32, i32* %min, align 4
  %pointer35 = getelementptr [10 x i32], [10 x i32]* @a, i32 0, i32 %min34
  %i36 = load i32, i32* %i, align 4
  %pointer37 = getelementptr [10 x i32], [10 x i32]* @a, i32 0, i32 %i36
  %a38 = load i32, i32* %pointer37, align 4
  store i32 %a38, i32* %pointer35, align 4
  %i39 = load i32, i32* %i, align 4
  %pointer40 = getelementptr [10 x i32], [10 x i32]* @a, i32 0, i32 %i39
  %temp41 = load i32, i32* %temp, align 4
  store i32 %temp41, i32* %pointer40, align 4
  br label %entry30

if_false29:                                       ; preds = %entry12
  br label %entry30

entry30:                                          ; preds = %if_false29, %if_true28
  %i42 = load i32, i32* %i, align 4
  %plus43 = add i32 %i42, 1
  store i32 %plus43, i32* %i, align 4
  br label %whileCondition
}
