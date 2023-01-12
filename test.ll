; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %i = alloca i32, align 4
  store i32 0, i32* %i, align 4
  %j = alloca i32, align 4
  store i32 0, i32* %j, align 4
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry20, %mainEntry
  %i1 = load i32, i32* %i, align 4
  %cmp = icmp slt i32 %i1, 10
  %cmp2 = zext i1 %cmp to i32
  %cmp3 = icmp ne i32 %cmp2, 0
  br i1 %cmp3, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %a4 = load i32, i32* %a, align 4
  %plus = add i32 %a4, 1
  store i32 %plus, i32* %a, align 4
  br label %whileCondition5

entry:                                            ; preds = %if_true, %whileCondition
  %a21 = load i32, i32* %a, align 4
  ret i32 %a21

whileCondition5:                                  ; preds = %whileBody10, %whileBody
  %j6 = load i32, i32* %j, align 4
  %cmp7 = icmp slt i32 %j6, 3
  %cmp8 = zext i1 %cmp7 to i32
  %cmp9 = icmp ne i32 %cmp8, 0
  br i1 %cmp9, label %whileBody10, label %entry11

whileBody10:                                      ; preds = %whileCondition5
  %a12 = load i32, i32* %a, align 4
  %plus13 = add i32 %a12, 1
  store i32 %plus13, i32* %a, align 4
  %j14 = load i32, i32* %j, align 4
  %plus15 = add i32 %j14, 1
  store i32 %plus15, i32* %j, align 4
  br label %whileCondition5

entry11:                                          ; preds = %whileCondition5
  %a16 = load i32, i32* %a, align 4
  %cmp17 = icmp eq i32 %a16, 5
  %cmp18 = zext i1 %cmp17 to i32
  %cmp19 = icmp ne i32 %cmp18, 0
  br i1 %cmp19, label %if_true, label %if_false

if_true:                                          ; preds = %entry11
  br label %entry
  br label %entry20

if_false:                                         ; preds = %entry11
  br label %entry20

entry20:                                          ; preds = %if_false, %if_true
  br label %whileCondition
}
