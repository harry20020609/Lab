; ModuleID = 'moudle'
source_filename = "moudle"

@a = global i32 0

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %whileBody, %mainEntry
  br i1 true, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %a1 = alloca i32, align 4
  store i32 2, i32* %a1, align 4
  br label %entry
  br label %whileCondition

entry:                                            ; preds = %whileBody, %whileCondition
  %a2 = load i32, i32* %a, align 4
  ret i32 %a2
}
