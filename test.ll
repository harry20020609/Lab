; ModuleID = 'moudle'
source_filename = "moudle"

@a = global i32 1
@b = global i32 2
@c = global i32 3

define i32 @main() {
mainEntry:
  %d = alloca i32, align 4
  store i32 0, i32* %d, align 4
  %a = load i32, i32* @a, align 4
  %b = load i32, i32* @b, align 4
  %cmp = icmp slt i32 %a, %b
  %cmp1 = zext i1 %cmp to i32
  %a2 = load i32, i32* @a, align 4
  %cmp3 = icmp slt i32 %cmp1, %a2
  %cmp4 = zext i1 %cmp3 to i32
  %cmp5 = icmp ne i32 %cmp4, 0
  br i1 %cmp5, label %if_true, label %if_false

if_true:                                          ; preds = %mainEntry
  store i32 1, i32* %d, align 4
  br label %entry

if_false:                                         ; preds = %mainEntry
  br label %entry

entry:                                            ; preds = %if_false, %if_true
  %d6 = load i32, i32* %d, align 4
  ret i32 %d6
}
