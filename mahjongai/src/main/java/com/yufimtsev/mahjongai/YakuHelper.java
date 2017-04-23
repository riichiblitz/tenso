package com.yufimtsev.mahjongai;

public class YakuHelper {

    private static int POSITION_COUNTER = 0;

    private static final int P_TANYAO_CLOSED = POSITION_COUNTER++;
    private static final int P_TANYAO_OPEN = POSITION_COUNTER++;
    private static final int P_PINFU = POSITION_COUNTER++;
    private static final int P_MENZEN = POSITION_COUNTER++;
    private static final int P_IPEIKO = POSITION_COUNTER++;
    private static final int P_RINSHAN = POSITION_COUNTER++;
    private static final int P_CHANKAN = POSITION_COUNTER++;
    private static final int P_HAITEI = POSITION_COUNTER++;
    private static final int P_HOTEI = POSITION_COUNTER++;
    private static final int P_RIICHI = POSITION_COUNTER++;
    private static final int P_IPPATSU = POSITION_COUNTER++;
    private static final int P_DABURI = POSITION_COUNTER++;
    private static final int P_CHITOITSU = POSITION_COUNTER++;
    private static final int P_SANSHOKU_CLOSED = POSITION_COUNTER++;
    private static final int P_SANSHOKU_OPEN = POSITION_COUNTER++;
    private static final int P_ITSU_CLOSED = POSITION_COUNTER++;
    private static final int P_ITSU_OPEN = POSITION_COUNTER++;
    private static final int P_CHANTA_CLOSED = POSITION_COUNTER++;
    private static final int P_CHANTA_OPEN = POSITION_COUNTER++;
    private static final int P_TOITOI = POSITION_COUNTER++;
    private static final int P_SANANKO = POSITION_COUNTER++;
    private static final int P_HONROTO = POSITION_COUNTER++;
    private static final int P_SANDOKO = POSITION_COUNTER++;
    private static final int P_SANKANTSU = POSITION_COUNTER++;
    private static final int P_SHOSANGEN = POSITION_COUNTER++;
    private static final int P_SANRENKO = POSITION_COUNTER++;
    private static final int P_RYANPEIKO = POSITION_COUNTER++;
    private static final int P_JUNCHAN_CLOSED = POSITION_COUNTER++;
    private static final int P_JUNCHAN_OPEN = POSITION_COUNTER++;
    private static final int P_HONITSU_CLOSED = POSITION_COUNTER++;
    private static final int P_HONITSU_OPEN = POSITION_COUNTER++;
    private static final int P_NAGASHI = POSITION_COUNTER++;
    private static final int P_RENHO = POSITION_COUNTER++;
    private static final int P_KOKUSHI = POSITION_COUNTER++;
    private static final int P_SUANKO = POSITION_COUNTER++;
    private static final int P_CHUREN = POSITION_COUNTER++;
    private static final int P_DAISHANRIN = POSITION_COUNTER++;
    private static final int P_SURENKO = POSITION_COUNTER++;
    private static final int P_PARENCHAN = POSITION_COUNTER++;
    private static final int P_DAISANGEN = POSITION_COUNTER++;
    private static final int P_TSUISO = POSITION_COUNTER++;
    private static final int P_SHOSUSHI = POSITION_COUNTER++;
    private static final int P_DAISUSHI = POSITION_COUNTER++;
    private static final int P_RYUISO = POSITION_COUNTER++;
    private static final int P_CHINRO = POSITION_COUNTER++;
    private static final int P_SUKANTSU = POSITION_COUNTER++;
    private static final int P_TENHO = POSITION_COUNTER++;
    private static final int P_CHIHO = POSITION_COUNTER++;

    private static final int[] HAN = new int[POSITION_COUNTER - 1];

    static {
        HAN[P_TANYAO_CLOSED] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_PINFU] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
        HAN[P_TANYAO_OPEN] = 1;
    }

    public static long[][] getValue(long[][] currentHand, long[][] checkHand) {
        long[][] result = new long[2][4];

        return result;
    }
}
