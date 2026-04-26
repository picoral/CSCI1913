public class UseStack {
    public static void main(String[] args) {
        Stack<String> myStack = new ArrayStack<>(10);
        //Stack<String> myStack = new LinkedStack<>();
        myStack.push("apple");
        myStack.push("pear");
        myStack.push("kiwi");
        myStack.push("grape");
        System.out.println(myStack);
        System.out.println(myStack.pop());
        System.out.println(myStack);
        System.out.println(myStack.top());
        System.out.println(myStack);
    }
}
