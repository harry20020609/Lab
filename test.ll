; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 0, i32* %a, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry12, %mainEntry
  %a1 = load i32, i32* %a, align 4
  %cmp = icmp slt i32 %a1, 5
  %cmp2 = zext i1 %cmp to i32
  %cmp3 = icmp ne i32 %cmp2, 0
  br i1 %cmp3, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %a4 = load i32, i32* %a, align 4
  %plus = add i32 %a4, 1
  store i32 %plus, i32* %a, align 4
  %a5 = alloca i32, align 4
  store i32 6, i32* %a5, align 4
  br label %whileCondition6

entry:                                            ; preds = %whileCondition
  %a15 = load i32, i32* %a, align 4
  ret i32 %a15

whileCondition6:                                  ; preds = %whileBody11, %whileBody
  %a7 = load i32, i32* %a5, align 4
  %cmp8 = icmp slt i32 %a7, 7
  %cmp9 = zext i1 %cmp8 to i32
  %cmp10 = icmp ne i32 %cmp9, 0
  br i1 %cmp10, label %whileBody11, label %entry12

whileBody11:                                      ; preds = %whileCondition6
  %a13 = load i32, i32* %a5, align 4
  %plus14 = add i32 %a13, 1
  store i32 %plus14, i32* %a5, align 4
  br label %whileCondition6

entry12:                                          ; preds = %whileCondition6
  br label %whileCondition
}
