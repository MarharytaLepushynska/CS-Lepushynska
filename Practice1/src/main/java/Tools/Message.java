package Tools;

public class Message {
    private byte bSrc;
    private long bPktId;
    private int cType;
    private int bUserId;
    private String message;

    public Message() {

    }

    public Message(byte bSrc, long bPktId, int cType, int bUserId, String message) {
        this.bSrc = bSrc;
        this.bPktId = bPktId;
        this.cType = cType;
        this.bUserId = bUserId;
        this.message = message;
    }

    public byte getbSrc() {
        return bSrc;
    }

    public void setbSrc(byte bSrc) {
        this.bSrc = bSrc;
    }

    public long getbPktId() {
        return bPktId;
    }

    public void setbPktId(long bPktId) {
        this.bPktId = bPktId;
    }

    public int getcType() {
        return cType;
    }

    public void setcType(int cType) {
        this.cType = cType;
    }

    public int getbUserId() {
        return bUserId;
    }

    public void setbUserId(int bUserId) {
        this.bUserId = bUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
