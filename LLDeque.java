package editor;

public class LLDeque<Item> {

    private int size; /*size of list */
    public OneNode cursor;
    private OneNode sentinel;
    public int currPos;

    public class OneNode {
        private Item item;
        public OneNode prev = null;
        public OneNode next = null;


        private OneNode(Item item0, OneNode prev0, OneNode next0) {
            item = item0;
            prev = prev0;
            next = next0;
        }
    }

    public LLDeque() {
        sentinel = new OneNode(null, null, null);
        cursor = new OneNode(null, sentinel, null);
        sentinel.next = cursor;
        size = 0;
        currPos = 0;
    }

    public OneNode getNext(OneNode n) {
        return n.next;
    }
    public void addPrev(Item i) {
        OneNode newprev = new OneNode(i, cursor.prev, cursor);
        cursor.prev.next = newprev;
        cursor.prev = newprev;
        size++;
        currPos++;
    }

    public OneNode removePrev() {
        OneNode removedNode = cursor.prev;
        if (cursor.prev != null && cursor.prev.prev != null) {
            cursor.prev.prev.next = cursor;
            cursor.prev = cursor.prev.prev;
            size--;
        }
        currPos--;
        return removedNode;
    }

    public int getCurrPos() {
        return currPos;
    }

//    public boolean checkWords() {
//
//    }

    public void cursorLeft() {
        OneNode temp = cursor.prev.prev;
        if (cursor.prev != null && cursor.prev.prev != null) {
            cursor.prev.next = cursor.next;
            if (cursor.next != null) {
                cursor.next.prev = cursor.prev;
            }
            cursor.prev.prev.next = cursor;
            cursor.prev.prev = cursor;
            cursor.next = cursor.prev;
            cursor.prev = temp;
            currPos --;
        }
    }

    public void cursorRight() {
        OneNode temp = cursor.next.next;
        if (cursor.next != null) {
            cursor.next.prev = cursor.prev;
            if (cursor.prev != null) {
                cursor.prev.next = cursor.next;
            }
            if (cursor.next.next != null) {
                cursor.next.next.prev = cursor;
                cursor.next.next = cursor;
            }
            cursor.prev = cursor.next;
            cursor.next = temp;
            currPos ++;
        }
    }


    public String getString(int index) {

        return (get(index).toString());
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.println(get(i));
        }
    }


    public Item get(int index) {

        if (this.size() < index) {
            return null;
        }
        OneNode counter = sentinel;
        while (index > -1) {
            if (counter.next == cursor) {
                index ++;
            }
            counter = counter.next;
            index--;
        }
        return counter.item;
    }

    public OneNode getNode(int index) {
        if (this.size() < index) {
            return null;
        }
        OneNode counter = sentinel;
        while (index > -1) {
            if (counter.next == cursor) {
                index ++;
            }
            counter = counter.next;
            index--;
        }
        return counter;
    }

    public Item getItemNode(OneNode n) {
        return n.item;
    }

    public OneNode getCursor() {
        int i = 0;
        OneNode n = sentinel;
        while (n != cursor) {
            n = n.next;
            i++;
        }
        currPos = i;
        return n;
    }

//    public OneNode getCursor() {
//        OneNode counter = sentinel;
//        if (counter.next == cursor) {
//            return counter.next;
//        } else {
//            counter = counter.next;
//        }
//    }


//    public static void main(String[] args) {
//        LLDeque test = new LLDeque();
//        test.addPrev(1);
//        test.addPrev(7);
//        test.addPrev(8);
//        test.addPrev(5);
//        test.getCursor();
//        System.out.print(test.currPos);
//    }

}