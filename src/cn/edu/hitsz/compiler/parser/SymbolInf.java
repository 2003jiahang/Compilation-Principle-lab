package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.NonTerminal;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;

public class SymbolInf {
    Token token = null;
    NonTerminal nonTerminal = null;
    SourceCodeType type = null;
    IRValue value = null;

    public SymbolInf(Token token, SourceCodeType type){
        this.token = token;
        this.type = type;
    }

    public SymbolInf(Token token){
        this.token = token;
        this.type = null;
    }

    public SymbolInf(NonTerminal nonTerminal, SourceCodeType type){
        this.nonTerminal = nonTerminal;
        this.type = type;
    }

    public SymbolInf(NonTerminal nonTerminal, IRValue value){
        this.nonTerminal = nonTerminal;
        this.value = value;
        this.type = null;
    }

    public SymbolInf(NonTerminal nonTerminal){
        this.nonTerminal = nonTerminal;
        this.type = null;
    }
}
