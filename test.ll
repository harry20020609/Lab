; ModuleID = 'moudle'
source_filename = "moudle"

@sort_arr = global [5 x i32] zeroinitializer

define i32 @combine(i32* %0, i32 %1, i32* %2, i32 %3) {
combineEntry:
  %arr1 = alloca i32*, align 8
  store i32* %0, i32** %arr1, align 8
  %arr1_length = alloca i32, align 4
  store i32 %1, i32* %arr1_length, align 4
  %arr2 = alloca i32*, align 8
  store i32* %2, i32** %arr2, align 8
  %arr2_length = alloca i32, align 4
  store i32 %3, i32* %arr2_length, align 4
  %i = alloca i32, align 4
  store i32 0, i32* %i, align 4
  %j = alloca i32, align 4
  store i32 0, i32* %j, align 4
  %k = alloca i32, align 4
  store i32 0, i32* %k, align 4
  br label %whileCondition

whileCondition:                                   ; preds = %entry10, %combineEntry
  %i1 = load i32, i32* %i, align 4
  %arr1_length2 = load i32, i32* %arr1_length, align 4
  %tmp_ = icmp slt i32 %i1, %arr1_length2
  %tmp_3 = zext i1 %tmp_ to i32
  %j4 = load i32, i32* %j, align 4
  %arr2_length5 = load i32, i32* %arr2_length, align 4
  %tmp_6 = icmp slt i32 %j4, %arr2_length5
  %tmp_7 = zext i1 %tmp_6 to i32
  %tmp_8 = and i32 %tmp_3, %tmp_7
  %tmp_9 = icmp ne i32 0, %tmp_8
  br i1 %tmp_9, label %whileBody, label %entry

whileBody:                                        ; preds = %whileCondition
  %i11 = load i32, i32* %i, align 4
  %arr112 = load i32*, i32** %arr1, align 8
  %res = getelementptr i32, i32* %arr112, i32 %i11
  %arr113 = load i32, i32* %res, align 4
  %j14 = load i32, i32* %j, align 4
  %arr215 = load i32*, i32** %arr2, align 8
  %res16 = getelementptr i32, i32* %arr215, i32 %j14
  %arr217 = load i32, i32* %res16, align 4
  %tmp_18 = icmp slt i32 %arr113, %arr217
  %tmp_19 = zext i1 %tmp_18 to i32
  %tmp_20 = icmp ne i32 0, %tmp_19
  br i1 %tmp_20, label %true, label %false

entry:                                            ; preds = %whileCondition
  %i42 = load i32, i32* %i, align 4
  %arr1_length43 = load i32, i32* %arr1_length, align 4
  %tmp_44 = icmp eq i32 %i42, %arr1_length43
  %tmp_45 = zext i1 %tmp_44 to i32
  %tmp_46 = icmp ne i32 0, %tmp_45
  br i1 %tmp_46, label %true39, label %false40

true:                                             ; preds = %whileBody
  %i21 = load i32, i32* %i, align 4
  %arr122 = load i32*, i32** %arr1, align 8
  %res23 = getelementptr i32, i32* %arr122, i32 %i21
  %arr124 = load i32, i32* %res23, align 4
  %k25 = load i32, i32* %k, align 4
  %res26 = getelementptr [5 x i32], [5 x i32]* @sort_arr, i32 0, i32 %k25
  store i32 %arr124, i32* %res26, align 4
  %i27 = load i32, i32* %i, align 4
  %tmp_28 = add i32 %i27, 1
  store i32 %tmp_28, i32* %i, align 4
  br label %entry10

false:                                            ; preds = %whileBody
  %j29 = load i32, i32* %j, align 4
  %arr230 = load i32*, i32** %arr2, align 8
  %res31 = getelementptr i32, i32* %arr230, i32 %j29
  %arr232 = load i32, i32* %res31, align 4
  %k33 = load i32, i32* %k, align 4
  %res34 = getelementptr [5 x i32], [5 x i32]* @sort_arr, i32 0, i32 %k33
  store i32 %arr232, i32* %res34, align 4
  %j35 = load i32, i32* %j, align 4
  %tmp_36 = add i32 %j35, 1
  store i32 %tmp_36, i32* %j, align 4
  br label %entry10

entry10:                                          ; preds = %false, %true
  %k37 = load i32, i32* %k, align 4
  %tmp_38 = add i32 %k37, 1
  store i32 %tmp_38, i32* %k, align 4
  br label %whileCondition

true39:                                           ; preds = %entry
  br label %whileCondition47

false40:                                          ; preds = %entry
  br label %whileCondition65

entry41:                                          ; preds = %entry67, %entry49
  %arr1_length83 = load i32, i32* %arr1_length, align 4
  %arr2_length84 = load i32, i32* %arr2_length, align 4
  %tmp_85 = add i32 %arr1_length83, %arr2_length84
  %tmp_86 = sub i32 %tmp_85, 1
  %res87 = getelementptr [5 x i32], [5 x i32]* @sort_arr, i32 0, i32 %tmp_86
  %sort_arr = load i32, i32* %res87, align 4
  ret i32 %sort_arr

whileCondition47:                                 ; preds = %whileBody48, %true39
  %j50 = load i32, i32* %j, align 4
  %arr2_length51 = load i32, i32* %arr2_length, align 4
  %tmp_52 = icmp slt i32 %j50, %arr2_length51
  %tmp_53 = zext i1 %tmp_52 to i32
  %tmp_54 = icmp ne i32 0, %tmp_53
  br i1 %tmp_54, label %whileBody48, label %entry49

whileBody48:                                      ; preds = %whileCondition47
  %j55 = load i32, i32* %j, align 4
  %arr256 = load i32*, i32** %arr2, align 8
  %res57 = getelementptr i32, i32* %arr256, i32 %j55
  %arr258 = load i32, i32* %res57, align 4
  %k59 = load i32, i32* %k, align 4
  %res60 = getelementptr [5 x i32], [5 x i32]* @sort_arr, i32 0, i32 %k59
  store i32 %arr258, i32* %res60, align 4
  %k61 = load i32, i32* %k, align 4
  %tmp_62 = add i32 %k61, 1
  store i32 %tmp_62, i32* %k, align 4
  %j63 = load i32, i32* %j, align 4
  %tmp_64 = add i32 %j63, 1
  store i32 %tmp_64, i32* %j, align 4
  br label %whileCondition47

entry49:                                          ; preds = %whileCondition47
  br label %entry41

whileCondition65:                                 ; preds = %whileBody66, %false40
  %i68 = load i32, i32* %i, align 4
  %arr1_length69 = load i32, i32* %arr1_length, align 4
  %tmp_70 = icmp slt i32 %i68, %arr1_length69
  %tmp_71 = zext i1 %tmp_70 to i32
  %tmp_72 = icmp ne i32 0, %tmp_71
  br i1 %tmp_72, label %whileBody66, label %entry67

whileBody66:                                      ; preds = %whileCondition65
  %i73 = load i32, i32* %i, align 4
  %arr274 = load i32*, i32** %arr2, align 8
  %res75 = getelementptr i32, i32* %arr274, i32 %i73
  %arr276 = load i32, i32* %res75, align 4
  %k77 = load i32, i32* %k, align 4
  %res78 = getelementptr [5 x i32], [5 x i32]* @sort_arr, i32 0, i32 %k77
  store i32 %arr276, i32* %res78, align 4
  %k79 = load i32, i32* %k, align 4
  %tmp_80 = add i32 %k79, 1
  store i32 %tmp_80, i32* %k, align 4
  %i81 = load i32, i32* %i, align 4
  %tmp_82 = add i32 %i81, 1
  store i32 %tmp_82, i32* %i, align 4
  br label %whileCondition65

entry67:                                          ; preds = %whileCondition65
  br label %entry41
}

define i32 @main() {
mainEntry:
  %a = alloca [2 x i32], align 4
  %pointer = getelementptr [2 x i32], [2 x i32]* %a, i32 0, i32 0
  store i32 1, i32* %pointer, align 4
  %pointer1 = getelementptr [2 x i32], [2 x i32]* %a, i32 0, i32 1
  store i32 5, i32* %pointer1, align 4
  %b = alloca [3 x i32], align 4
  %pointer2 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 0
  store i32 1, i32* %pointer2, align 4
  %pointer3 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 1
  store i32 4, i32* %pointer3, align 4
  %pointer4 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 2
  store i32 14, i32* %pointer4, align 4
  %res = getelementptr [2 x i32], [2 x i32]* %a, i32 0, i32 0
  %res5 = getelementptr [3 x i32], [3 x i32]* %b, i32 0, i32 0
  %combine = call i32 @combine(i32* %res, i32 2, i32* %res5, i32 3)
  ret i32 %combine
}
