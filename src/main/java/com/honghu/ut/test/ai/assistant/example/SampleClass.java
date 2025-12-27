package com.honghu.ut.test.ai.assistant.example;

/**
 * 示例类
 * 用于演示 UT AI 助手插件功能的示例类
 * 
 * 这个类包含各种类型的方法，用于测试插件的单元测试生成功能
 */
public class SampleClass {
    private String name;
    private int value;

    public SampleClass(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int calculateSum(int a, int b) {
        return a + b;
    }

    public int calculateProduct(int a, int b) {
        return a * b;
    }

    public boolean isPositive(int number) {
        return number > 0;
    }

    public String processText(String input) {
        if (input == null) {
            return "NULL";
        }
        if (input.trim().isEmpty()) {
            return "EMPTY";
        }
        return input.toUpperCase();
    }

    public int[] getPositiveNumbers(int[] numbers) {
        if (numbers == null) {
            return new int[0];
        }
        
        int count = 0;
        for (int num : numbers) {
            if (num > 0) {
                count++;
            }
        }
        
        int[] result = new int[count];
        int index = 0;
        for (int num : numbers) {
            if (num > 0) {
                result[index++] = num;
            }
        }
        
        return result;
    }
}