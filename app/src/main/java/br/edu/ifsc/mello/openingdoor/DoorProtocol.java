package br.edu.ifsc.mello.openingdoor;

/**
 * Created by mello on 01/06/16.
 */
public enum DoorProtocol {
    HELLO((short) 1, "HELLO"),
    READY((short) 2, "READY"),
    WAIT((short) 3, "WAIT"),
    ERROR((short) 4, "ERROR"),
    DONE((short) 5, "DONE"),
    GRANTED((short) 6, "GRANTED"),
    READER_ERROR((short) 7, "READER_ERROR"),
    DENY((short) 8, "DENY"),
    BYE((short) 9, "BYE"),
    NEXT((short) 10, "NEXT"),
    OK((short) 11, "OK"),
    SUCCESS((short) 12, "SUCCESS"),
    RESPONSE((short) 13, "RESPONSE"),
    RESULT((short) 14, "RESULT");

    private final short id;
    private final String desc;

    DoorProtocol(final short id, final String desc) {
        this.id = id;
        this.desc = desc;
    }

    public static DoorProtocol byValue(String val) {
        for (DoorProtocol en : values()) {
            if (en.desc.equals(val)) {
                return en;
            }
        }
        throw new IllegalArgumentException("Invalid uaf intent type description: " + val);
    }

    public short getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }
}
