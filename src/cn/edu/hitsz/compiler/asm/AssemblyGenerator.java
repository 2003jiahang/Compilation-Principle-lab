package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    List<Instruction> instructions = new ArrayList<>();

    List<Register> registers = new ArrayList<>();

    Map<IRVariable, Register> variableToRegisterMap = new HashMap<>();

    private final List<String> asmInstructions = new ArrayList<>(List.of(".text"));


    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        for (Instruction instruction : originInstructions){
            InstructionKind instructionKind = instruction.getKind();
            if (instructionKind.isReturn()){
                instructions.add(instruction);
                break;
            } else if (instructionKind.isUnary()) {
                instructions.add(instruction);
            } else if (instructionKind.isBinary()) {
                IRValue lhs = instruction.getLHS();
                IRValue rhs = instruction.getRHS();
                IRVariable result = instruction.getResult();
                // 如果两个操作数都是立即数：将操作两个立即数的BinaryOp直接进行求值得到结果，然后替换成MOV指令
                if (lhs.isImmediate() && rhs.isImmediate()){
                    int immediateResult = 0;
                    switch (instructionKind){
                        case ADD -> immediateResult = ((IRImmediate)lhs).getValue() + ((IRImmediate)rhs).getValue();
                        case SUB -> immediateResult = ((IRImmediate)lhs).getValue() - ((IRImmediate)rhs).getValue();
                        case MUL -> immediateResult = ((IRImmediate)lhs).getValue() * ((IRImmediate)rhs).getValue();
                        default -> System.out.println("error");
                    }
                    instructions.add(Instruction.createMov(result, IRImmediate.of(immediateResult)));
                }else if (lhs.isImmediate() || rhs.isImmediate()){
                    IRVariable temp = IRVariable.temp();
                    if(lhs.isImmediate()){
                        instructions.add(Instruction.createMov(temp, lhs));
                        switch (instructionKind){
                            case ADD -> instructions.add(Instruction.createAdd(result, temp, rhs));
                            case SUB -> instructions.add(Instruction.createSub(result, temp, rhs));
                            case MUL -> instructions.add(Instruction.createMul(result, temp, rhs));
                            default -> System.out.println("error");
                        }
                    }else{
                        instructions.add(Instruction.createMov(temp, rhs));
                        switch (instructionKind){
                            case ADD -> instructions.add(Instruction.createAdd(result, lhs, temp));
                            case SUB -> instructions.add(Instruction.createSub(result, lhs, temp));
                            case MUL -> instructions.add(Instruction.createMul(result, lhs, temp));
                            default -> System.out.println("error");
                        }
                    }
                }else{
                    instructions.add(instruction);
                }
            }
        }
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        for (int i=0; i<=6; i++){
            String name = "t" + i;
            registers.add(new Register(name,i));
        }
        String asmCode = null;
        for (Instruction instruction:instructions){
            InstructionKind instructionKind = instruction.getKind();
            if(instructionKind.isUnary()){ //Mov
                int tempResultRegister;
                int tempFromRegister;
                if(instruction.getResult().isTemp()){
                    tempResultRegister = setRegisters(true);
                    variableToRegisterMap.put(instruction.getResult(), registers.get(tempResultRegister));
                }else{
                    tempResultRegister = setRegisters(false);
                    variableToRegisterMap.put(instruction.getResult(), registers.get(tempResultRegister));
                }
                registers.get(tempResultRegister).setUse();

                if(instruction.getFrom().isIRVariable()){
                    tempFromRegister = variableToRegisterMap.get((IRVariable)instruction.getFrom()).getNum();
                    if(((IRVariable) instruction.getFrom()).isTemp()){
                        registers.get(tempFromRegister).relUse();
                        variableToRegisterMap.remove((IRVariable) instruction.getFrom());
                    }
                    asmCode = String.format("\tmv %s, %s", registers.get(tempResultRegister).getName(), registers.get(tempFromRegister).getName());
                }else{
                    asmCode = String.format("\tli %s, %s", registers.get(tempResultRegister).getName(), instruction.getFrom().toString());
                }
                asmInstructions.add(asmCode);

            } else if (instructionKind.isReturn()) { //Return
                IRVariable returnValue = (IRVariable) instruction.getReturnValue();
                Register returnRegister = variableToRegisterMap.get(returnValue);
                asmCode = String.format("\tmv a0, %s", returnRegister.getName());
                asmInstructions.add(asmCode);

            } else if (instructionKind.isBinary()){ // ADD, SUB, MUL
                int tempResultRegister;
                if(instruction.getResult().isTemp()){
                    tempResultRegister = setRegisters(true);
                    variableToRegisterMap.put(instruction.getResult(), registers.get(tempResultRegister));
                }else{
                    tempResultRegister = setRegisters(false);
                    variableToRegisterMap.put(instruction.getResult(), registers.get(tempResultRegister));
                }
                registers.get(tempResultRegister).setUse();

                String LhsRegister = variableToRegisterMap.get((IRVariable)instruction.getLHS()).getName();
                String RhsRegister = variableToRegisterMap.get((IRVariable)instruction.getRHS()).getName();
                switch (instructionKind){
                    case ADD -> asmCode = String.format("\tadd %s, %s, %s", registers.get(tempResultRegister).getName(), LhsRegister, RhsRegister);
                    case SUB -> asmCode = String.format("\tsub %s, %s, %s", registers.get(tempResultRegister).getName(), LhsRegister, RhsRegister);
                    case MUL -> asmCode = String.format("\tmul %s, %s, %s", registers.get(tempResultRegister).getName(), LhsRegister, RhsRegister);
                    default -> System.out.println("error");
                }
                asmInstructions.add(asmCode);
                if(((IRVariable) instruction.getLHS()).isTemp()){
                    variableToRegisterMap.get((IRVariable)instruction.getLHS()).relUse();
                    variableToRegisterMap.remove((IRVariable)instruction.getLHS());
                }
                if(((IRVariable) instruction.getRHS()).isTemp()){
                    variableToRegisterMap.get((IRVariable)instruction.getRHS()).relUse();
                    variableToRegisterMap.remove((IRVariable)instruction.getRHS());
                }

            }
        }
    }


    private int setRegisters(boolean fromTop){
        if (fromTop){
            for (int i=0;i<=6;i++){
                Register register = registers.get(i);
                if (!register.isUse()) return i;
            }
        }else{
            for (int i=6;i>=0;i--){
                Register register = registers.get(i);
                if (!register.isUse()) return i;
            }
        }
        throw new RuntimeException("Dont have enough register");
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines(path, asmInstructions);
    }
}

