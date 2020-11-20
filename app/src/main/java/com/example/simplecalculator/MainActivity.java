package com.example.simplecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    enum NumbersState {zero, decimal, floating, negative, result};

    TextView memoryLabel, resultLabel;
    Button clearButton, clearEntityButton, backspaceButton, memoryButton;

    NumbersState numbersState = NumbersState.zero;

    String memory = "";

    boolean initialValue = true, zeroValue = false;

    private void initUIElements(){
        memoryLabel = findViewById(R.id.memoryLabel);
        resultLabel = findViewById(R.id.resultLabel);

        clearButton = findViewById(R.id.clear);
        clearEntityButton = findViewById(R.id.clearEntity);
        backspaceButton = findViewById(R.id.backspace);
        memoryButton = findViewById(R.id.memory);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUIElements();

        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = resultLabel.getText().toString();
                Log.d("hoss", "onClick: "+str);
                // remove error message
                if(str.equals("error")){
                    resultLabel.setText("0");
                    memoryLabel.setText("0");
                    numbersState = NumbersState.zero;
                    initialValue = true;
                }
                // delete last character
                else if(str.length() > 1){
                    if(str.charAt(str.length()-1) == '.')
                        numbersState = NumbersState.decimal;
                    resultLabel.setText(str.substring(0, str.length()-1));
                }
                // reset to 0
                else if(str.length() == 1){
                    resultLabel.setText("0");
                    numbersState = NumbersState.zero;
                }
            }
        });

        clearEntityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clear input field
                resultLabel.setText("0");
                numbersState = NumbersState.zero;
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clear equation, result, state -> initial
                resultLabel.setText("0");
                memoryLabel.setText("0");
                numbersState = NumbersState.zero;
                initialValue = true;
            }
        });

        memoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(memory.length() > 1){
                    // recall last calculated value
                    String mem = memoryLabel.getText().toString();
                    // handle negative results
                    if(memory.charAt(0) == '-' && mem.charAt(mem.length()-1) != '-'){
                        if(initialValue)
                            resultLabel.setText(memory);
                        else {
                            append("-" ,memoryLabel);
                            resultLabel.setText(memory.substring((1)));
                        }
                    }
                    else {
                        resultLabel.setText(memory);
                    }
                    // decide next state
                    if(memory.contains("."))
                        numbersState = NumbersState.floating;
                    else
                        numbersState = NumbersState.decimal;
                }
            }
        });
    }

    private boolean isBtnOp(String btn){
        return btn.equals("+") || btn.equals("-") || btn.equals("×") || btn.equals("÷") || btn.equals("=");
    }

    private void append(String str, TextView target){
        if(target.equals(memoryLabel)){
            if(memoryLabel.getText().toString().length() < 30)
                memoryLabel.append(str);
            else
                Toast.makeText(getApplicationContext(), "Max Equation Limit reached!\nPress C, then M to restart with the current result", Toast.LENGTH_LONG).show();
        }
        else {
            if(!str.equals("21474.83647") && !str.equals("2147483647") && !str.equals("-2147483647"))
                resultLabel.setText(str);
            else{
                Toast.makeText(getApplicationContext(), "Overflow!\nPlease restart with smaller numbers", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void preventLabelOverFlow(String str){
        if(resultLabel.getText().toString().length() < 9)
            resultLabel.append(str);
        else
            Toast.makeText(getApplicationContext(), "Max limit reached!", Toast.LENGTH_LONG).show();
    }

    private void saveResult(String result) {
        if(!result.equals("21474.83647") && !result.equals("2147483647") && !result.equals("-2147483647"))
            memory = result;
    }

    private String reduceEquation(String equation){
        Log.d("hoss", "in: " + equation);

        // find high precedence operator
        int mpos = equation.indexOf("×");
        int dpos = equation.indexOf("÷");
        int pos = 0;
        char op = '×';
        if(mpos != -1 && dpos != -1){
            pos = Math.min(dpos, mpos);
            if(equation.charAt(pos) != op)
                op = '÷';
        }
        else if(dpos != -1) {
            pos = dpos;
            op = '÷';
        }
        else
            pos = mpos;

        Log.d("hoss", "pos: " + pos);

        // get right hand side number
        String rhs = "";
        int i = pos+1;

        //eliminate duplications of - signs like +---
        int opNumber = 1;
        while(equation.charAt(i) == '+' || equation.charAt(i) == '-'){
            if(equation.charAt(i) == '-')
                opNumber *= -1;
            i++;
        }
        if(opNumber == -1)
            rhs += "-";
        char cc = equation.charAt(i);
        while(!isBtnOp(cc+"") && i < equation.length()){
            rhs += cc;
            if(++i < equation.length())
                cc = equation.charAt(i);
        }

        //get left had side number
        String lhs = "";
        int j = pos-1;
        cc = equation.charAt(j);
        while(!isBtnOp(cc+"") && j >= 0) {
            lhs += cc;
            if (--j >= 0)
                cc = equation.charAt(j);
        }

        char[] lhss = lhs.toCharArray();
        lhs = "";
        for(int k=lhss.length-1; k >=0; k--)
            lhs += lhss[k];

        // perform calculation
        double value1 = Double.parseDouble(lhs);
        double value2 = Double.parseDouble(rhs);
        double result = 0;

        Log.d("hoss", "lhs: " + value1);
        Log.d("hoss", "rhs: " + value2);

        if(op == '×')
            result = value1*value2;
        else if(op == '÷'){
            if(value2 == 0)
                return "error";
            result = value1/value2;
        }

        // mask only 5 precision points
        String resultString = (result*10)%10 == 0? (int)result + "": ((int)(result*100000))/(double)100000 + "";

        String leftSide = equation.substring(0, j+1);
        String rightSide = equation.substring(i);
        equation = leftSide + resultString + rightSide;

        Log.d("hoss", "out: " + equation);

        // solve the rest of operators by recursion
        if(equation.contains("÷") || equation.contains("×"))
            return reduceEquation(equation);
        else {
            // all operators are calculated save the result
            saveResult(equation);
            return equation;
        }
    }

    private String performAddition(String lhs, String rhs, char op){
        //perform and mask result of + or -
        double l = Double.parseDouble(lhs);
        double r = Double.parseDouble(rhs);
        double res = 0;
        if(op == '+')
            res = l + r;
        else
            res = l - r;
        return  (res*10)%10 == 0? (int)res + "": ((int)(res*100000))/(double)100000 + "";
    }

    private String addUpEquation(String equation){
        String lhs = "", rhs = "", result = "";
        int i = 0;
        char op = 'n';

        Log.d("hoss", "in: " + equation);

        //1. start by -
        if(equation.charAt(0) == '-'){
            // --number, remove --
            if(equation.charAt(1) == '-') return addUpEquation(equation.substring(2));

            i = equation.indexOf("+", 1);
            if(i == -1)
                i = equation.indexOf("-", 1);
            //2. just a negative number -23 no equation or --23
            if(i == -1){
                saveResult(equation);
                return equation;
            }

            // 1. continuous starts by a negative number
            lhs =  equation.substring(0, i);
            Log.d("hoss", "lhs: " + lhs);
        }
        //3. start by a positive number
        else {
            int nsign = equation.indexOf("-", 1);
            int psign = equation.indexOf("+", 1);
            if(nsign != -1 && psign != -1)
                i = Math.min(nsign, psign);
            else if(nsign != -1)
                i = nsign;
            else if(psign != -1)
                i = psign;
            //just a number
            else
                return equation;

            // 1. continuous starts by a positive number
            lhs = equation.substring(0, i);
            Log.d("hoss", "lhs: " + lhs);
        }

        //4. figure out the op: -, +, +-, -- ...
        // preserve i, as the pos of the last op
        int opNumber = 1;
        while(equation.charAt(i) == '+' || equation.charAt(i) == '-'){
            if(equation.charAt(i) == '-')
                opNumber *= -1;
            i++;
        }
        op = opNumber == 1? '+' : '-';
        Log.d("hoss", "op: " + op);

        //get rhs
        int psign = equation.indexOf('+', i);
        int nsign = equation.indexOf('-', i);
        int k = 0;
        //end of equation
        if(psign == -1 && nsign == -1) {
            rhs = equation.substring(i);
            Log.d("hoss", "rhs: " + rhs);
            result = performAddition(lhs, rhs, op);
            Log.d("hoss", "out: " + result);
            saveResult(result);
            return result;
        }
        //more operators, reduce and recall
        else if(nsign != -1 && psign == -1)
            k = nsign;
        else if(nsign == -1)
            k = psign;
        else
            k = Math.min(psign, nsign);

        rhs = equation.substring(i, k);
        Log.d("hoss", "rhs: " + rhs);

        result = performAddition(lhs, rhs, op);
        equation = result + equation.substring(k);
        Log.d("hoss", "out: " + equation);
        return addUpEquation(equation);
    }

    // solve equation by reduction of operations with higher precedence
    private String parseEquation(String equation) {
        // remove last operator from the equation: 3x49-2x
        if(isBtnOp(equation.charAt(equation.length()-1)+"")){
            equation = equation.substring(0, equation.length()-1);
        }
        // solve and reduce multiplications and divisions
        if(equation.contains("×") || equation.contains("÷")){
            equation = reduceEquation(equation);
        }
        // solve and reduce additions and subtractions
        if(equation.contains("+") || equation.contains("-")){
            equation = addUpEquation(equation);
        }
        return equation;
    }

    private void setStateVariablesOnResult(String resultText, String newOp){
        String str = "";
        // start a new equation and save the first number and operator
        if(initialValue){
            // ignore equal clicks on empty equations
            if(!newOp.equals("=")){
                initialValue = false;
                str = resultText + newOp;
                memoryLabel.setText(str);
                resultLabel.setText("0");
                numbersState = NumbersState.zero;
            }
        }
        // continue calculation on existing equation
        else {
            // equal clicked -> display result
            if(newOp.equals("=")){
                String s = memoryLabel.getText().toString();
                // append equation, display result
                if(isBtnOp(s.charAt(s.length()-1)+"")){
                    append(resultText, memoryLabel);
                    String result = parseEquation(memoryLabel.getText().toString());
                    append(result, resultLabel);
                }
                // clear equation
                else {
                    memoryLabel.setText("0");
                    initialValue = true;
                }
            }
            // continue, append equation and display result
            else {
                String mem = memoryLabel.getText().toString();
                if(isBtnOp(mem.charAt(mem.length()-1)+""))
                    str = resultText + newOp;
                else
                    str = newOp;
                append(str, memoryLabel);
                String result = parseEquation(memoryLabel.getText().toString());
                append(result, resultLabel);
                numbersState = NumbersState.zero;
            }
        }
    }

    public void buttonClicked(View view){
        String btn = ((Button)view).getText().toString();
        String mem = memoryLabel.getText().toString();
        String lastMem = mem.charAt(mem.length()-1) + "";

        switch (numbersState){
            case zero:
                // accept numbers or negative sign, transition to corresponding state
                if(!isBtnOp(btn) || btn.equals("-")){
                    if(btn.equals("-") && mem.equals("0")){
                        resultLabel.setText("-");
                        numbersState = NumbersState.negative;
                    }
                    else if(btn.equals(".")){
                        resultLabel.setText("0.");
                        numbersState = NumbersState.floating;
                    }
                    else if(!btn.equals("-")){
                        append(btn, resultLabel);
                        if(!btn.equals("0"))
                            numbersState = NumbersState.decimal;
                        else
                            zeroValue = true;
                    }
                }
                // allow change of operation, - then +, replace - with +
                if(isBtnOp(btn) && !btn.equals("=") && isBtnOp(lastMem)){
                    if(btn.equals("-") && !isBtnOp(mem.charAt(mem.length()-2)+"")){
                        append(btn, memoryLabel);
                    }
                    else if(!isBtnOp(mem.charAt(mem.length()-2)+"")){
                        mem = mem.substring(0, mem.length()-1) + btn;
                        memoryLabel.setText(mem);
                    }
                }
                // accept 0 as input to the equation
                if(btn.equals("=") && zeroValue && !initialValue){
                    append("0", memoryLabel);
                    append(parseEquation(memoryLabel.getText().toString()), resultLabel);
                    zeroValue = false;
                }
                break;

            case negative:
                // negative case just add - sign
                if(!(btn.equals("0") || isBtnOp(btn))){
                    if(btn.equals(".")){
                        resultLabel.setText("-0.");
                        numbersState = NumbersState.floating;
                    }
                    else{
                        preventLabelOverFlow(btn);
                        numbersState = NumbersState.decimal;
                    }
                }
                else if(isBtnOp(btn) && !btn.equals("=") && isBtnOp(lastMem)){
                    mem = lastMem + btn;
                    memoryLabel.setText(mem);
                }
                break;

            case decimal:
                // append or transition to floating
                if(isBtnOp(btn)){
                    setStateVariablesOnResult(resultLabel.getText().toString(), btn);
                }
                else {
                    preventLabelOverFlow(btn);
                    if(btn.equals(".")) {
                        numbersState = NumbersState.floating;
                    }
                }
                break;

            case floating:
                // append and prevent additional floating points "."
                if(isBtnOp(btn)){
                    setStateVariablesOnResult(resultLabel.getText().toString(), btn);
                }
                else if(!btn.equals("."))
                    preventLabelOverFlow(btn);
                break;

            case result:
                // accept op and wait for another number
                if(isBtnOp(btn) && !btn.equals("=")){
                    append(btn, memoryLabel);
                    numbersState = NumbersState.zero;
                }
                else if(btn.equals("0")){
                    resultLabel.setText("0");
                    numbersState = NumbersState.zero;
                }
                else if(btn.equals(".")){
                    resultLabel.setText("0.");
                    numbersState = NumbersState.floating;
                }
                else {
                    resultLabel.setText(btn);
                    numbersState = NumbersState.decimal;
                }
        }
    }
}