package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.LRTable;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.Objects;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    private SymbolTable symbolTable;
    private final Stack<SymbolInf> tokenInf = new Stack<>();

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        switch(production.index()) {
            case 4:     //S -> D id;
                SymbolInf symbolInf1 = tokenInf.pop(); //id
                SymbolInf symbolInf2 = tokenInf.pop(); //D
                symbolTable.get(symbolInf1.token.getText()).setType(symbolInf2.type);
                tokenInf.push(new SymbolInf(production.head()));
                break;
            case 5:    //D -> int
                SymbolInf symbolInf = tokenInf.pop(); //int
                tokenInf.push(new SymbolInf(production.head(), symbolInf.type));
                break;
            default:
                for (int i = 0; i < production.body().size(); i++) {
                    tokenInf.pop();
                }
                tokenInf.push(new SymbolInf((production.head())));
        }

    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        SymbolInf inf;
        if(Objects.equals(currentToken.getKindId(), "int")){
            inf = new SymbolInf(currentToken, SourceCodeType.Int);
        }else {
            inf = new SymbolInf(currentToken);
        }
        tokenInf.add(inf);

    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.symbolTable = table;
    }

}

