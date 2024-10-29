package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    List<Instruction> ir = new ArrayList<>();
    SymbolTable table;
    Stack<SymbolInf> symbolInfStack = new Stack<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        SymbolInf symbolInf = new SymbolInf(currentToken);
        if(Objects.equals(currentToken.getKindId(), "$") || Objects.equals(currentToken.getKindId(), "id")){
            symbolInf.value = IRVariable.named(currentToken.getText());
        }
        if(Objects.equals(currentToken.getKindId(), "IntConst")){
            symbolInf.value = IRImmediate.of(Integer.parseInt(currentToken.getText()));
        }
        symbolInfStack.push(symbolInf);
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        SymbolInf curSym1, curSym2;
        IRVariable temp;
        switch(production.index()){
            case 6: //S -> id = E;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.pop();
                curSym2 = symbolInfStack.pop();
                ir.add(Instruction.createMov((IRVariable) curSym2.value, curSym1.value));
                symbolInfStack.push(new SymbolInf(production.head()));
                break;
            case 7: //S -> return E;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.pop();
                ir.add(Instruction.createRet(curSym1.value));
                symbolInfStack.push(new SymbolInf(production.head()));
                break;
            case 8: //E -> E + A;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.pop();
                curSym2 = symbolInfStack.pop();
                temp = IRVariable.temp();
                ir.add(Instruction.createAdd(temp, curSym2.value, curSym1.value));
                symbolInfStack.push(new SymbolInf(production.head(), temp));
                break;
            case 9: //E -> E - A;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.pop();
                curSym2 = symbolInfStack.pop();
                temp = IRVariable.temp();
                ir.add(Instruction.createSub(temp, curSym2.value, curSym1.value));
                symbolInfStack.push(new SymbolInf(production.head(), temp));
                break;
            case 10: //E -> A;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.push(new SymbolInf(production.head(), curSym1.value));
                break;
            case 11: //A -> A * B;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.pop();
                curSym2 = symbolInfStack.pop();
                temp = IRVariable.temp();
                ir.add(Instruction.createMul(temp, curSym2.value, curSym1.value));
                symbolInfStack.push(new SymbolInf(production.head(), temp));
                break;
            case 12: //A -> B;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.push(new SymbolInf(production.head(), curSym1.value));
                break;
            case 13: //B -> ( E );
                symbolInfStack.pop();
                curSym1 = symbolInfStack.pop();
                symbolInfStack.pop();
                symbolInfStack.push(new SymbolInf(production.head(), curSym1.value));
                break;
            case 14: //B -> id;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.push(new SymbolInf(production.head(), curSym1.value));
                break;
            case 15: //B -> IntConst;
                curSym1 = symbolInfStack.pop();
                symbolInfStack.push(new SymbolInf(production.head(), curSym1.value));
                break;
            default:
                for (int i = 0; i < production.body().size(); i++) {
                    symbolInfStack.pop();
                }
                symbolInfStack.push(new SymbolInf((production.head())));

        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.table = table;
    }

    public List<Instruction> getIR() {
        // TODO
        return ir;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

