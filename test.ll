; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 0, i32* %a, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry9, %mainEntry
  %a1 = load i32, i32* %a, align 4
  %cmp = icmp slt i32 %a1, 9
  %cmp2 = zext i1 %cmp to i32
  %cmp3 = icmp ne i32 %cmp2, 0
  br i1 %cmp3, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %a4 = load i32, i32* %a, align 4
  %plus = add i32 %a4, 1
  store i32 %plus, i32* %a, align 4
  %a5 = load i32, i32* %a, align 4
  %cmp6 = icmp eq i32 %a5, 9
  %cmp7 = zext i1 %cmp6 to i32
  %cmp8 = icmp ne i32 %cmp7, 0
  br i1 %cmp8, label %if_true, label %if_false

entry:                                            ; preds = %if_true, %whileCondition
  %a10 = load i32, i32* %a, align 4
  ret i32 %a10

if_true:                                          ; preds = %whileBody
  br label %entry
  br label %entry9

if_false:                                         ; preds = %whileBody
  br label %entry9

entry9:                                           ; preds = %if_false, %if_true
  br label %whileCondition
}
