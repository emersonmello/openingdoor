package br.edu.ifsc.mello.openingdoor;

/**
 * Created by mello on 01/06/16.
 */
public enum DoorProtocol {
    HELLO((short) 1, "1"),
    READY((short) 2, "2"),
    WAIT((short) 3, "3"),
    ERROR((short) 4, "4"),
    DONE((short) 5, "5"),
    GRANTED((short) 6, "6"),
    DENY((short) 7, "7");

    private final short id;
    private final String desc;

    DoorProtocol(final short id, final String desc){
        this.id = id;
        this.desc = desc;
    }

    public short getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }
}
