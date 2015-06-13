package net.virtualinfinity.telnet;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetConstants {
    public static final byte SE        = (byte)240;
    public static final byte NOP       = (byte)241;
    public static final byte DATA_MARK = (byte)242;
    public static final byte BRK       = (byte)243;
    public static final byte IP        = (byte)244;
    public static final byte AO        = (byte)245;
    public static final byte AYT       = (byte)246;
    public static final byte EC        = (byte)247;
    public static final byte EL        = (byte)248;
    public static final byte GA        = (byte)249;
    public static final byte SB        = (byte)250;
    public static final byte WILL      = (byte)251;
    public static final byte WONT      = (byte)252;
    public static final byte DO        = (byte)253;
    public static final byte DONT      = (byte)254;
    public static final byte IAC       = (byte)255;

    public static String name(byte input) {
        switch (input) {
            case SE       : return "SE";
            case NOP      : return "NOP";
            case DATA_MARK: return "DATA_MARK";
            case BRK      : return "BRK";
            case IP       : return "IP";
            case AO       : return "AO";
            case AYT      : return "AYT";
            case EC       : return "EC";
            case EL       : return "EL";
            case GA       : return "GA";
            case SB       : return "SB";
            case WILL     : return "WILL";
            case WONT     : return "WONT";
            case DO       : return "DO";
            case DONT     : return "DONT";
            case IAC      : return "IAC";
        }

        return null;
    }
}
