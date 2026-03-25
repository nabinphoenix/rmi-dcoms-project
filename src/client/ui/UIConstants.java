package client.ui;

import java.awt.*;

public class UIConstants {
    // Colors - Global Strategy
    public static final Color COLOR_BG_PAGE      = new Color(0xF0F2F5);
    public static final Color COLOR_BG_CARD      = Color.WHITE;
    public static final Color COLOR_BORDER       = new Color(0xE2E5EA);
    
    // Chat Colors
    public static final Color COLOR_BUBBLE_SENT_BG = new Color(0x2980B9);
    public static final Color COLOR_BUBBLE_SENT_FG = Color.WHITE;
    public static final Color COLOR_BUBBLE_RECV_BG = new Color(0xF1F4F8);
    public static final Color COLOR_BUBBLE_RECV_FG = new Color(0x1F2937);
    public static final Color COLOR_TEXT_MUTED     = new Color(0x9CA3AF);
    public static final Color COLOR_TEXT_DARK      = new Color(0x1F2937);
    public static final Color COLOR_TEXT_HINT      = new Color(0x9CA3AF);
    public static final Color COLOR_AVATAR_BG      = new Color(0x2980B9);

    // Table Colors
    public static final Color COLOR_TABLE_HEADER_BG = new Color(0xF8FAFC);
    public static final Color COLOR_TABLE_HEADER_FG = new Color(0x6B7280);
    public static final Color COLOR_TABLE_SELECTION_BG = new Color(0xDBEEFF);
    public static final Color COLOR_TABLE_HOVER_BG     = new Color(0xF0F7FF);
    public static final Color COLOR_TABLE_ACCENT       = new Color(0x2980B9);
    public static final Color COLOR_TABLE_ROW_BORDER   = new Color(0xF3F4F6);

    // Button Colors
    public static final Color COLOR_BTN_ADD    = new Color(0x27AE60);
    public static final Color COLOR_BTN_REMOVE = new Color(0xE05C4A);
    public static final Color COLOR_BTN_SUBMIT = new Color(0x2980B9);

    // Navigation
    public static final Color COLOR_NAV_ACTIVE   = new Color(0x2980B9);
    public static final Color COLOR_NAV_INACTIVE = new Color(0x6B7280);

    // Legacy Aliases & Missing Constants for backward compatibility
    public static final Color COLOR_PRIMARY      = COLOR_BTN_SUBMIT;
    public static final Color COLOR_SUCCESS      = COLOR_BTN_ADD;
    public static final Color COLOR_DANGER       = COLOR_BTN_REMOVE;
    public static final Color COLOR_BORDER_INPUT = new Color(0xD1D5DB);
    public static final Color COLOR_BG_TOPBAR    = new Color(0x1E2A38);
    public static final Color COLOR_BG_NAV       = new Color(0xFFFFFF);
    public static final Color COLOR_SELECTION_BG = COLOR_TABLE_SELECTION_BG;
    public static final Color COLOR_ACCENT       = COLOR_TABLE_ACCENT;

    // Fonts
    // Fonts - EXACT Reference
    public static final Font FONT_BODY          = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_CHAT_META     = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_AVATAR        = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font FONT_TABLE_HEADER  = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font FONT_BTN           = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_NAV           = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_TOPBAR        = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SECTION       = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL         = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_LABEL_M       = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_VALUE         = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_METRIC        = new Font("Segoe UI", Font.BOLD, 38);

    // Badge Colors (Leave Status)
    public static final Color BADGE_APPROVED_FG = new Color(0x15803D);
    public static final Color BADGE_APPROVED_BG = new Color(0xDCFCE7);
    public static final Color BADGE_REJECTED_FG = new Color(0xB91C1C);
    public static final Color BADGE_REJECTED_BG = new Color(0xFEE2E2);
    public static final Color BADGE_PENDING_FG  = new Color(0x92400E);
    public static final Color BADGE_PENDING_BG  = new Color(0xFEF3C7);
}
