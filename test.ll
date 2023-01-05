; ModuleID = 'moudle'
source_filename = "moudle"

define i32 @main() {
mainEntry:
  %a = alloca i32, align 4
  store i32 1, i32* %a, align 4
  %b = alloca i32, align 4
  store i32 2, i32* %b, align 4
  %c = alloca i32, align 4
  store i32 3, i32* %c, align 4
  %a1 = load i32, i32* %a, align 4
  %tmp = icmp ne i32 %a1, 0
  %tmp2 = zext i1 %tmp to i32
  %b3 = load i32, i32* %b, align 4
  %tmp4 = icmp ne i32 %b3, 0
  %tmp5 = zext i1 %tmp4 to i32
  %cmp = icmp eq i32 %tmp2, %tmp5
  %cmp6 = zext i1 %cmp to i32
  %cmp7 = icmp ne i32 %cmp6, 0
  br i1 %cmp7, label %if_true, label %if_false

if_true:                                          ; preds = %mainEntry
  store i32 1, i32* %c, align 4
  br label %entry

if_false:                                         ; preds = %mainEntry
  store i32 0, i32* %c, align 4
  br label %entry

entry:                                            ; preds = %if_false, %if_true
  %c8 = load i32, i32* %c, align 4
  ret i32 %c8
}
