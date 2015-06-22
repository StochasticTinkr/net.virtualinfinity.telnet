package net.virtualinfinity.telnet;

/**
 * <a href="http://www.iana.org/assignments/telnet-options/telnet-options.xhtml">Telnet Options</a> as defined by IANA
 * as of June 22nd, 2015.
 *
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public enum Option implements HasOptionCode {
    BINARY_TRANSMISSION(0),
    ECHO(1),
    RECONNECTION(2),
    SUPPRESS_GO_AHEAD(3),
    APPROX_MESSAGE_SIZE_NEGOTIATION(4),
    STATUS(5),
    TIMING_MARK(6),
    REMOTE_CONTROLLED_TRANS_AND_ECHO(7),
    OUTPUT_LINE_WIDTH(8),
    OUTPUT_PAGE_SIZE(9),
    OUTPUT_CARRIAGE_RETURN_DISPOSITION(10),
    OUTPUT_HORIZONTAL_TAB_STOPS(11),
    OUTPUT_HORIZONTAL_TAB_DISPOSITION(12),
    OUTPUT_FORM_FEED_DISPOSITION(13),
    OUTPUT_VERTICAL_TAB_STOPS(14),
    OUTPUT_VERTICAL_TAB_DISPOSITION(15),
    OUTPUT_LINE_FEED_DISPOSITION(16),
    EXTENDED_ASCII(17),
    LOGOUT(18),
    BYTE_MACRO(19),
    DATA_ENTRY_TERMINAL(20),
    SUPDUP(21),
    SUPDUP_OUTPUT(22),
    SEND_LOCATION(23),
    TERMINAL_TYPE(24),
    END_OF_RECORD(25),
    TACACS_USER_IDENTIFICATION(26),
    OUTPUT_MARKING(27),
    TERMINAL_LOCATION_NUMBER(28),
    TELNET_3270_REGIME(29),
    X_3_PAD(30),
    NEGOTIATE_ABOUT_WINDOW_SIZE(31),
    TERMINAL_SPEED(32),
    REMOTE_FLOW_CONTROL(33),
    LINEMODE(34),
    X_DISPLAY_LOCATION(35),
    ENVIRONMENT_OPTION(36),
    AUTHENTICATION_OPTION(37),
    ENCRYPTION_OPTION(38),
    NEW_ENVIRONMENT_OPTION(39),
    TN3270E(40),
    XAUTH(41),
    CHARSET(42),
    TELNET_REMOTE_SERIAL_PORT(43),
    COM_PORT_CONTROL_OPTION(44),
    TELNET_SUPRESS_LOCAL_ECHO(45),
    TELNET_START_TLS(46),
    KERMIT(47),
    SEND_URL(48),
    FORWARD_X(49),
    // 50 - 137 Unassigned
    TELOPT_PRAGMA_LOGON(138),
    TELOPT_SSPI_LOGON(139),
    TELOPT_PRAGMA_HEARTBEAT(140),
    // 141-254 Unassigned
    EXTENDED_OPTIONS_LIST(255),
    ;
    private static final Option[] byId = new Option[256];
    static {
        for (final Option option: values()) {
            byId[option.optionCode()] = option;
        }
    }
    private final int optionCode;

    Option(int optionCode) {
        this.optionCode = optionCode;
    }

    @Override
    public int optionCode() {
        return optionCode;
    }

    public static Option byId(int optionId) {
        if (optionId >= 0 && optionId < byId.length) {
            return byId[optionId];
        }

        return null;
    }
}
