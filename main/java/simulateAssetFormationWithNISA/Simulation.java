package java.SimulateAssetFormationWithNISA; //このファイルが所属するフォルダ

import java.simulateAssetFormationWithNISA.HomeController.SimulationParams;  //HomeControllerファイルのクラスrecordを読み込む宣言

import java.math.BigDecimal;  //少数を正確に扱う金融計算のためのクラス
import java.math.RoundingMode;  //小数点の丸め方指定
import java.util.*;  //java.utilパッケージ内のクラスを全て使えるようにする

//シミュレーション繰り返し回数
public class Simulation {
    static int simuNum = 10000;

    
     /**
     * 入力値からブラックショールズモデルを用いて、将来の資産額を算出します
     * @param params 入力値
     * @return グラフの値
     */
    public static List<List<Double>> getValuationData(SimulationParams params) {
        
        // 運用月数、イベント発生月のマップ
        Map<String, Integer> monthElement = new HashMap<>() {
            {
                if (params.advancedSetting().endingAge() != 0) {
                    put("monthCount", (params.advancedSetting().endingAge() - params.startAge()) * 12);
                } else {
                    put("monthCount", (65 - params.startAge()) * 12);
                }
                put("monthOfLifeEvent1", (params.lifeEventParams().lifeEventAge1() - params.startAge()) * 12);
                put("monthOfLifeEvent2", (params.lifeEventParams().lifeEventAge2() - params.startAge()) * 12);
                put("monthOfLifeEvent3", (params.lifeEventParams().lifeEventAge3() - params.startAge()) * 12);
                put("monthOfLifeEvent4", (params.lifeEventParams().lifeEventAge4() - params.startAge()) * 12);
                put("monthOfLifeEvent5", (params.lifeEventParams().lifeEventAge5() - params.startAge()) * 12);
            }
        };

        // N回分のシナリオ作成
        List<List<Double>> simuArr = new ArrayList<>();
        createScenario(params, simuArr, monthElement);

        // VaRのシナリオ作成
        List<List<Double>> VaR = new ArrayList<>();
        createVaR(params, simuArr, VaR, monthElement);

        return VaR;
    }


    /**
     * シミュレーション回数分のシナリオを作成します
     *
     * @param params 入力値
     * @param simuArr シミュレーション回数分のシナリオ
     * @param monthElement 運用月数andイベント発生月
     */
    private static void createScenario(SimulationParams params, List<List<Double>> simuArr, Map<String, Integer> monthElement) {
        Random random = new Random(); //Randomをインスタンス化

        double expectedRateOfReturn = params.expectedRateOfReturn() / 100; //期待収益率を % → 小数 に変換
        double volatility = params.volatility() / 100; //ボラティリティを % → 小数 に変換

        // N回シミュレーション
        for (int n = 0; n < simuNum; n++) {
            List<Double> scenario = new ArrayList<>(); //一回分の月ごとの残高リスト作成
            double monthlySavings =  params.monthlySavings(); //現在の月の積立額
            double totalReserveAmount = 0; //積立額トータル(上限1800)
            scenario.add(params.initialValue() + params.monthlySavings()); //積立1か月目の残高 「初期資産＋1か月分の積立」
            double limitRevival = 0; //翌年の非課税投資枠復活分
            for (int i = 1; i < monthElement.get("monthCount"); i++) {  //1か月ずつ増やしていく
                double delta = scenario.get(i - 1) * (expectedRateOfReturn / 12 + volatility * random.nextGaussian() / Math.sqrt(12) + 0);
                // 「今月の増分 = 前月の残高 * 月次リターン」

                // 非課税投資枠復活
                if (i % 12 == 0) {
                    totalReserveAmount -= limitRevival;
                    limitRevival = 0;
                }

                // 積立額トータル
                if (totalReserveAmount < 1800) { //生涯の非課税保有限度額が1800万円
                    totalReserveAmount += monthlySavings;
                }

                // 年変化
                if (params.advancedSetting().annualChangeMonth() != 0 && i % params.advancedSetting().annualChangeMonth() == 0) {
                    monthlySavings += params.advancedSetting().annualChangeMoney();
                }

                double percentageOfPrincipal = totalReserveAmount / scenario.get(i-1); // 元本の割合計算（今月の資産のうち元本がどのくらいの割合か） 「積み立てた元本の合計/前月までの総資産（元本＋運用益）」
                if (i == monthElement.get("monthOfLifeEvent1")) {
                    limitRevival += params.lifeEventParams().requiredFunds1() * percentageOfPrincipal; //非課税枠復活の計算（翌年のNISA非課税枠が復活する金額として記録） 「必要資金 * 元本の割合をlimitRevivalに加算」
                    if (totalReserveAmount < 1800) {
                        scenario.add(scenario.get(i - 1) + delta + monthlySavings - params.lifeEventParams().requiredFunds1()); //「前年資産＋今月の運用増分＋今月の積立－ライフイベント必要資金」
                    } else {
                        scenario.add(scenario.get(i - 1) + delta - params.lifeEventParams().requiredFunds1());
                    }
                } else if (i == monthElement.get("monthOfLifeEvent2")) {
                    limitRevival += params.lifeEventParams().requiredFunds2() * percentageOfPrincipal;
                    if (totalReserveAmount < 1800) {
                        scenario.add(scenario.get(i - 1) + delta + monthlySavings - params.lifeEventParams().requiredFunds2());
                    } else {
                        scenario.add(scenario.get(i - 1) + delta - params.lifeEventParams().requiredFunds2());
                    }
                } else if (i == monthElement.get("monthOfLifeEvent3")) {
                    limitRevival += params.lifeEventParams().requiredFunds3() * percentageOfPrincipal;
                    if (totalReserveAmount < 1800) {
                        scenario.add(scenario.get(i - 1) + delta + monthlySavings - params.lifeEventParams().requiredFunds3());
                    } else {
                        scenario.add(scenario.get(i - 1) + delta - params.lifeEventParams().requiredFunds3());
                    }
                } else if (i == monthElement.get("monthOfLifeEvent4")) {
                    limitRevival += params.lifeEventParams().requiredFunds4() * percentageOfPrincipal;
                    if (totalReserveAmount < 1800) {
                        scenario.add(scenario.get(i - 1) + delta + monthlySavings - params.lifeEventParams().requiredFunds4());
                    } else {
                        scenario.add(scenario.get(i - 1) + delta - params.lifeEventParams().requiredFunds4());
                    }
                } else if (i == monthElement.get("monthOfLifeEvent5")) {
                    limitRevival += params.lifeEventParams().requiredFunds5() * percentageOfPrincipal;
                    if (totalReserveAmount < 1800) {
                        scenario.add(scenario.get(i - 1) + delta + monthlySavings - params.lifeEventParams().requiredFunds5());
                    } else {
                        scenario.add(scenario.get(i - 1) + delta - params.lifeEventParams().requiredFunds5());
                    }
                } else {
                    if (totalReserveAmount < 1800) {
                        scenario.add(scenario.get(i - 1) + delta + monthlySavings);
                    } else {
                        scenario.add(scenario.get(i - 1) + delta);
                    }
                }
            }
            simuArr.add(scenario);
        }
    }


     /**
     * 不確実性のシナリオを作成します
     *
     * @param params 入力値
     * @param simuArr シミュレーション回数分のシナリオ
     * @param VaR 不確実性のシナリオ
     * @param monthElement 運用月数andイベント発生月
     */
    private static void createVaR(SimulationParams params, List<List<Double>> simuArr, List<List<Double>> VaR, Map<String, Integer> monthElement) {
        List<Double> top30Percent = new ArrayList<>();
        List<Double> median = new ArrayList<>();
        List<Double> bottom30Percent = new ArrayList<>();
        List<Double> bottom10Percent = new ArrayList<>();
        List<Double> noOperation = new ArrayList<>();

        for (int i = 0; i < monthElement.get("monthCount"); i++) {
            // 月ごとの値を取得
            List<Double> monthlyValue = new ArrayList<>();
            for (List<Double> sce : simuArr) {
                monthlyValue.add(sce.get(i)); //iか月目の資産残高リスト（N個の値）
            }

            // 上位30％、中央値、下位10％、下位30％
            getVaR(monthlyValue, top30Percent, median, bottom30Percent, bottom10Percent);

            // 運用なし
            getNoOperation(i, params, monthElement, noOperation);
        }

        //VaRリストへの格納
        VaR.add(top30Percent);
        VaR.add(median);
        VaR.add(bottom30Percent);
        VaR.add(bottom10Percent);
        VaR.add(noOperation);
    }

    /**
     * 月ごとの平均値を取得？？
     *
     * @param monthlyValue シミュレーション回数分のiか月目のリスト
     * @param expectedAverage 予想平均のシナリオ
     */
    private static void getExpectedAverage(List<Double> monthlyValue, List<Double> expectedAverage) {
        double average = monthlyValue.stream()
                .mapToDouble(a -> a)
                .average()
                .orElse(0);
        BigDecimal bdAverage = getRoundingOffNum(average);
        average = Double.parseDouble(String.valueOf(bdAverage));
        expectedAverage.add(average);
    }

    /**
     * 上位30％、中央値、下位10％、下位30%のシナリオ
     *
     * @param monthlyValue シミュレーション回数分のiか月目のリスト
     * @param top30Percent 上位30％のシナリオ
     * @param median 中央値シナリオ
     * @param bottom30Percent 下位30％のシナリオ
     * @param bottom10Percent 下位10％のシナリオ
     */
    private static void getVaR(List<Double> monthlyValue, List<Double> top30Percent, List<Double> median, List<Double> bottom30Percent, List<Double> bottom10Percent) {
        Collections.sort(monthlyValue); //値を小さい順にソート
        BigDecimal bdTop30 = getRoundingOffNum(monthlyValue.get(simuNum / 10 * 7));
        double top30 = Double.parseDouble(String.valueOf(bdTop30));
        top30Percent.add(top30);

        BigDecimal bdMedian = getRoundingOffNum(monthlyValue.get(simuNum / 10 * 5));
        double mdn = Double.parseDouble(String.valueOf(bdMedian));
        median.add(mdn);

        BigDecimal bdBottom30 = getRoundingOffNum(monthlyValue.get(simuNum / 10 * 3));
        double bottom30 = Double.parseDouble(String.valueOf(bdBottom30));
        bottom30Percent.add(bottom30);

        BigDecimal bdBottom10 = getRoundingOffNum(monthlyValue.get(simuNum / 10));
        double bottom10 = Double.parseDouble(String.valueOf(bdBottom10));
        bottom10Percent.add(bottom10);
    }

    /**
     * 運用なしのシナリオ
     *
     * @param i iか月目
     * @param params 入力値
     * @param monthElement 運用月数andイベント発生月
     * @param noOperation 運用なしのシナリオ
     */
    private static void getNoOperation(int i, SimulationParams params, Map<String, Integer> monthElement, List<Double> noOperation) {
        double totalReserveAmount = params.monthlySavings(); //月々の積立額を初期化
        //1か月目は初期積立額だけを追加、それ以降は積立額を足して計算
        if (i == 0) {
            noOperation.add(params.monthlySavings());
        } else {
            totalReserveAmount += params.monthlySavings();
            //ライフイベント処理
            if (i == monthElement.get("monthOfLifeEvent1")) {
                if (totalReserveAmount <= 1800) {
                    noOperation.add(noOperation.get(i - 1) + params.monthlySavings() - params.lifeEventParams().requiredFunds1());
                } else {
                    noOperation.add(noOperation.get(i - 1) - params.lifeEventParams().requiredFunds1());
                }
            } else if (i == monthElement.get("monthOfLifeEvent2")) {
                if (totalReserveAmount <= 1800) {
                    noOperation.add(noOperation.get(i - 1) + params.monthlySavings() - params.lifeEventParams().requiredFunds2());
                } else {
                    noOperation.add(noOperation.get(i - 1) - params.lifeEventParams().requiredFunds2());
                }
            } else if (i == monthElement.get("monthOfLifeEvent3")) {
                if (totalReserveAmount <= 1800) {
                    noOperation.add(noOperation.get(i - 1) + params.monthlySavings() - params.lifeEventParams().requiredFunds3());
                } else {
                    noOperation.add(noOperation.get(i - 1) - params.lifeEventParams().requiredFunds3());
                }
            } else if (i == monthElement.get("monthOfLifeEvent4")) {
                if (totalReserveAmount <= 1800) {
                    noOperation.add(noOperation.get(i - 1) + params.monthlySavings() - params.lifeEventParams().requiredFunds4());
                } else {
                    noOperation.add(noOperation.get(i - 1) - params.lifeEventParams().requiredFunds4());
                }
            } else if (i == monthElement.get("monthOfLifeEvent5")) {
                if (totalReserveAmount <= 1800) {
                    noOperation.add(noOperation.get(i - 1) + params.monthlySavings() - params.lifeEventParams().requiredFunds5());
                } else {
                    noOperation.add(noOperation.get(i - 1) - params.lifeEventParams().requiredFunds5());
                }
            } else {
                if (totalReserveAmount <= 1800) {
                    noOperation.add(noOperation.get(i - 1) + params.monthlySavings());
                } else {
                    noOperation.add(noOperation.get(i - 1));
                }
            }
        }
    }

    
     /**
     * 運用月数と年齢の対応リストを返す
     *
     * @param params 入力値
     * @return 運用月数と年齢の対応リスト
     */
    public static List<String> getAgeCountList(SimulationParams params) { //年齢と運用月数を対応させる、グラフのX軸用
        List<String> ageCountList = new ArrayList<>();

        // 運用月数
        int monthCount;
        if (params.advancedSetting().endingAge() != 0) {
            monthCount =  (params.advancedSetting().endingAge() - params.startAge()) * 12;
        } else {
            monthCount = (65 - params.startAge()) * 12;
        }

        //月ごとの年齢を作成
        int age = params.startAge();
        for (int i = 1; i < monthCount+1; i++) {
            if (i % 12 == 0) {
                age++; //12か月ごとに年齢を一つ増やす
            }
            ageCountList.add(age + "歳");
        }

        return ageCountList;
    }
    

    /**
     * 縦軸の最大値を返す
     *
     * @param valuationData グラフの値
     * @return 縦軸の最大値
     */
    public static double getSuggestedMax(List<List<Double>> valuationData) { //グラフ描画の縦軸（Y軸）の最大値を決定する
        double suggestedMax = 0;
        for (List<Double> data : valuationData) {
            for (double value : data) {
                if (value > suggestedMax) {
                    suggestedMax = value; //最終的に全シナリオの最大値を返す
                }
            }
        }
        return suggestedMax;
    }

    /**
     * 目盛り線の幅を返す
     *
     * @param suggestedMax 縦軸の最大値
     * @return 目盛り線の幅
     */
    public static int getStepSize(double suggestedMax) { //グラフのY軸の目盛間隔を自動設定する、最大値に応じて目盛り線の幅を決定
        int stepSize;
        if (suggestedMax < 1000) {
            stepSize = 100;
        } else if (suggestedMax < 20000) {
            stepSize = 1000;
        } else if (suggestedMax < 50000) {
            stepSize = 2000;
        } else if (suggestedMax < 100000) {
            stepSize = 5000;
        } else if (suggestedMax < 200000) {
            stepSize = 10000;
        } else if (suggestedMax < 500000) {
            stepSize = 20000;
        } else if (suggestedMax < 1000000) {
            stepSize = 50000;
        } else if (suggestedMax < 2000000) {
            stepSize = 100000;
        } else if (suggestedMax < 5000000) {
            stepSize = 200000;
        } else if (suggestedMax < 10000000) {
            stepSize = 500000;
        } else {
            stepSize = 1000000;
        }
        return stepSize;
    }

    /**
     * 小数第3位を四捨五入
     *
     * @param num 評価額
     * @return 四捨五入後の値
     */
    private static BigDecimal getRoundingOffNum(double num) { //金額などの評価額を小数第2位で四捨五入
        BigDecimal bdNum = new BigDecimal(num);
        return bdNum.setScale(2, RoundingMode.HALF_UP);
    }
}
