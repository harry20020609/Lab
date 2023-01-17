; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  %tmp = load i32, i32* %a, align 4
  %a1 = load i32, i32* %a, align 4
  %cmp = icmp eq i32 %a1, 1
  %cmp2 = zext i1 %cmp to i32
  %cmp3 = icmp ne i32 %cmp2, 0
  br i1 %cmp3, label %if_true, label %if_false

if_true:                                          ; preds = %mainEntry
  ret i32 1
  br label %entry

if_false:                                         ; preds = %mainEntry
  ret i32 2
  br label %entry

entry:                                            ; preds = %if_false, %if_true
  %space = alloca i32, align 4
  ret i32 0
}
