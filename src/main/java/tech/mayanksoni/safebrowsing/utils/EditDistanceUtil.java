package tech.mayanksoni.safebrowsing.utils;

public class EditDistanceUtil {
    public static int computeLevenshteinDistance(String s1, String s2) {
        int m = s1.length(), n = s2.length();

        //Ensure s1 smaller for smaller matrix
        if (m > n) {
            String temp = s1;
            s1 = s2;
            s2 = temp;
            m = s1.length();
            n = s2.length();
        }

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int i = 0; i <= m; i++) prev[i] = i;

        for (int j = 1; j <= n; j++) {
            curr[0] = j;
            for (int i = 1; i <= m; i++) {
                int insert = curr[i - 1] + 1;
                int delete = prev[i] + 1;
                int replace = prev[i - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1);
                curr[i] = Math.min(insert, Math.min(delete, replace));
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[m];
    }
}
