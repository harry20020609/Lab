; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %i = alloca i32, align 4
  store i32 0, i32* %i, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry8, %mainEntry
  %i1 = load i32, i32* %i, align 4
  %cmp = icmp slt i32 %i1, 9
  %cmp2 = zext i1 %cmp to i32
  %cmp3 = icmp ne i32 %cmp2, 0
  br i1 %cmp3, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %i4 = load i32, i32* %i, align 4
  %cmp5 = icmp eq i32 %i4, 7
  %cmp6 = zext i1 %cmp5 to i32
  %cmp7 = icmp ne i32 %cmp6, 0
  br i1 %cmp7, label %if_true, label %if_false

entry:                                            ; preds = %whileCondition
  ret void

if_true:                                          ; preds = %whileBody
  ret i32 1
  br label %entry8

if_false:                                         ; preds = %whileBody
  br label %entry8

entry8:                                           ; preds = %if_false, %if_true
  %i9 = load i32, i32* %i, align 4
  %plus = add i32 %i9, 1
  store i32 %plus, i32* %i, align 4
  br label %whileCondition
}
