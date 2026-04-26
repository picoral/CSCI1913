public class Node<T> {
    private T data;
    private Node<T> next;

    public Node(T data) {
        this.data = data;
    }

    public T getData() { return data; }
    public Node<T> getNext() { return next; }

    public void setData(T data) { this.data = data; }
    public void setNext(Node<T> node) { next = node; }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof Node<?> node) return data.equals(node.getData());
        return false;
    }

}
