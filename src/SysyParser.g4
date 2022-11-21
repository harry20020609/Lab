grammar SysyParser;

prog : ;

compUnit: compUnit? (decl | funcDef);

decl: constDecl | varDecl;

constDecl: 'const' bType constDef (','constDef)* ';';

bType: 'int';

constDef: Ident ('['constExp']')* '=' constInitVal;

constInitVal: constExp
            | '{'(constInitVal(','constInitVal)*)? '}';

varDecl: bType varDef (','varDef)* ';';

varDef: Ident ('['constExp']')*
      | Ident ('['constExp']') '=' initVal;

initVal: exp
       | '{' (initVal (','initVal)*)? '}';

funcDef: funcType Ident '(' (funcFParams)? ')' block;

funcType: 'void'
        | 'int';

funcFParams: funcFParam (',' funcFParam)*;

funcFParam: bType Ident ('['']'('['exp']')*)?;

block: '{' (blockItem)* '}';

blockItem: decl | stmt;

stmt: lVal '=' exp ';'
    | exp? ';'
    | block
    | 'if' '('cond')' stmt ('else' stmt)?
    | 'while' '(' cond ')' stmt
    | 'break' ';' | 'continue' ';'
    | 'return' (exp)? ';';

exp: addExp;

cond: lOrExp;

lVal: Ident ('['exp']')*;

primaryExp: '(' exp ')' | lVal | number;

number: IntConst;

unaryExp: primaryExp | Ident '(' (funcRParams)? ')' | unaryOp unaryExp;

unaryOp: '+' | '−' | '!';

funcRParams: exp (','exp)*;

mulExp: unaryExp | mulExp ('*' | '/' | '%') unaryExp;

addExp: mulExp | addExp ('+' | '−') mulExp;

relExp: addExp | relExp ('<' | '>' | '<=' | '>=') addExp;

eqExp: relExp | eqExp ('==' | '!=') relExp;

lAndExp: eqExp | lAndExp '&&' eqExp;

lOrExp: lAndExp | lOrExp '||' lAndExp;

constExp: addExp;








