//ファイル元
package java.simulateAssetFormationWithNISA;

//Spring Frameworkの機能を使う
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
//Java標準ライブラリのimport
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class HomeController {
                          
    record lifeEventsParams(
      String lifeEvent1, int lifeEventAge1, double requiredFunds1, 
      String lifeEvent2, int lifeEventAge2, double requiredFunds2, 
      String lifeEvent3, int lifeEventAge3, double requiredFunds3, 
      String lifeEvent4, int lifeEventAge4, double requiredFunds4, 
      String lifeEvent5, int lifeEventAge5, double requiredFunds5) {}
    record advancedSetting(int annualChangeMonth, int annualChangeMoney, int endingAge) {}
    record SimulationParams(String id, double expectedRateOfReturn, double volatility, int startAge, double monthlySavings, double initialValue, lifeEventParams lifeEventParams, advancedSetting advancedSetting) {}
    private String id;
    private double expectedRateOfReturn;
    private double volatility;
    private int startAge;
    private double monthlySavings;
    private double initialValue;
    private lifeEventParams lifeEventParams;
    private advancedSetting advancedSetting;
    private SimulationParams params = new SimulationParams(id, expectedRateOfReturn, volatility, startAge, monthlySavings, initialValue, lifeEventParams, advancedSetting);
    private lifeEventValidation lifeEventValidMessage = new lifeEventValidation();
    private advancedSettingValidation advancedSettingValidMessage = new advancedSettingValidation();
    private Validation validMessage = new Validation();
     boolean validateFlg = false;
    List<List<Double>> valuationData;
    List<String> countList;
    double suggestedMax;
    int stepSize;
    @RequestMapping(value="/mainpage")
    String mainpage(Model model) {
        model.addAttribute("time", LocalDateTime.now());
        return "mainpage";
    }
    
    @GetMapping("/list")
    String listItems(Model model) {
        if (params.id() != null) {
            if (validateFlg) {
                model.addAttribute("params", params);
                int i = 0;
                for (List<Double> data : valuationData) {
                    switch (i) {
                        case 0 -> model.addAttribute("top10Percent", data);
                        case 1 -> model.addAttribute("top30Percent", data);
//                        case 2 -> model.addAttribute("expectedAverage", data);
                        case 2 -> model.addAttribute("bottom30Percent", data);
                        case 3 -> model.addAttribute("bottom10Percent", data);
                        case 4 -> model.addAttribute("noOperation", data);
                    }
                    i++;
                }
                model.addAttribute("monthCountList", countList);
                model.addAttribute("suggestedMax", suggestedMax);
                model.addAttribute("stepSize", stepSize);
            } else {
                model.addAttribute("params", params);
                valuationData = Simulation.getValuationData(params);
                int i = 0;
                for (List<Double> data : valuationData) {
                    switch (i) {
                        case 0 -> model.addAttribute("top30Percent", data);
                        case 1 -> model.addAttribute("median", data);
//                        case 2 -> model.addAttribute("expectedAverage", data);
                        case 2 -> model.addAttribute("bottom30Percent", data);
                        case 3 -> model.addAttribute("bottom10Percent", data);
                        case 4 -> model.addAttribute("noOperation", data);
                    }
                    i++;
                }
                countList = Simulation.getAgeCountList(params);
                model.addAttribute("monthCountList", countList);
                suggestedMax = Simulation.getSuggestedMax(valuationData);
                model.addAttribute("suggestedMax", suggestedMax);
                stepSize = Simulation.getStepSize(suggestedMax);
                model.addAttribute("stepSize", stepSize);
            }
        }
        model.addAttribute("expectedRateOfReturnError", validMessage.expectedRateOfReturnError);
        model.addAttribute("volatilityError", validMessage.volatilityError);
        model.addAttribute("startAgeError", validMessage.startAgeError);
        model.addAttribute("monthlySavingsError", validMessage.monthlySavingsError);
        model.addAttribute("initialValueError", validMessage.initialValueError);
        model.addAttribute("lifeEventAge1Error", lifeEventValidMessage.lifeEventAge1Error);
        model.addAttribute("requiredFunds1Error", lifeEventValidMessage.requiredFunds1Error);
        model.addAttribute("lifeEventAge2Error", lifeEventValidMessage.lifeEventAge2Error);
        model.addAttribute("requiredFunds2Error", lifeEventValidMessage.requiredFunds2Error);
        model.addAttribute("lifeEventAge3Error", lifeEventValidMessage.lifeEventAge3Error);
        model.addAttribute("requiredFunds3Error", lifeEventValidMessage.requiredFunds3Error);
        model.addAttribute("lifeEventAge4Error", lifeEventValidMessage.lifeEventAge4Error);
        model.addAttribute("requiredFunds4Error", lifeEventValidMessage.requiredFunds4Error);
        model.addAttribute("lifeEventAge5Error", lifeEventValidMessage.lifeEventAge5Error);
        model.addAttribute("requiredFunds5Error", lifeEventValidMessage.requiredFunds5Error);
        model.addAttribute("annualChangeMoneyError", advancedSettingValidMessage.annualChangeMoneyError);
        model.addAttribute("endingAgeError", advancedSettingValidMessage.endingAgeError);

        return "mainpage";
    }

  
    @GetMapping("/add") //Spring MVCのアノテーション　HTTP GETリクエストの/addにアクセスしたときにこのメソッドを呼び出す
    String addItem(@RequestParam("expectedRateOfReturn") String requestExpectedRateOfReturn, @RequestParam("volatility") String requestVolatility, //addItemメソッドの宣言、@RequestParam()はURLパラメータを取得するためのアノテーション
                   @RequestParam("startAge") String requestStartAge, @RequestParam("monthlySavings") String requestMonthlySavings,
                   @RequestParam("initialValue") String requestInitialValue,
                   @RequestParam("lifeEvent1") String lifeEvent1, @RequestParam("lifeEventAge1") String requestLifeEventAge1, @RequestParam("requiredFunds1") String requestRequiredFunds1,
                   @RequestParam("lifeEvent2") String lifeEvent2, @RequestParam("lifeEventAge2") String requestLifeEventAge2, @RequestParam("requiredFunds2") String requestRequiredFunds2,
                   @RequestParam("lifeEvent3") String lifeEvent3, @RequestParam("lifeEventAge3") String requestLifeEventAge3, @RequestParam("requiredFunds3") String requestRequiredFunds3,
                   @RequestParam("lifeEvent4") String lifeEvent4, @RequestParam("lifeEventAge4") String requestLifeEventAge4, @RequestParam("requiredFunds4") String requestRequiredFunds4,
                   @RequestParam("lifeEvent5") String lifeEvent5, @RequestParam("lifeEventAge5") String requestLifeEventAge5, @RequestParam("requiredFunds5") String requestRequiredFunds5,
                   @RequestParam("annualChangePeriod") String annualChangePeriod, @RequestParam("annualChangeMoney") String requestAnnualChangeMoney,
                   @RequestParam("endingAge") String requestEndingAge) {
        String id = UUID.randomUUID().toString().substring(0, 8); //ランダムなIDを生成
        try { //パラメータを数値に変換して処理
            // 必須項目　文字列を数値に Double.parseDouble小数、Integer.parseInt整数
            double expectedRateOfReturn = Double.parseDouble(requestExpectedRateOfReturn);
            double volatility = Double.parseDouble(requestVolatility);
            int startAge = Integer.parseInt(requestStartAge);
            double monthlySavings = Double.parseDouble(requestMonthlySavings);
            double initialValue = Double.parseDouble(requestInitialValue);

            // ライフプラン　初期化、後でフォームがから出ない場合のみ上書きする
            int lifeEventAge1 = 0; double requiredFunds1 = 0;
            int lifeEventAge2 = 0; double requiredFunds2 = 0;
            int lifeEventAge3 = 0; double requiredFunds3 = 0;
            int lifeEventAge4 = 0; double requiredFunds4 = 0;
            int lifeEventAge5 = 0; double requiredFunds5 = 0;
            int annualChangeMonth = 0; int annualChangeMoney = 0;
            int endingAge = 0;

            //ライフイベント　空でなければ数値に変換して変数にセット、フォームに入力されなった項目は0のまま
            if (!requestLifeEventAge1.equals("") && !requestRequiredFunds1.equals("")) {
                lifeEventAge1 = Integer.parseInt(requestLifeEventAge1);
                requiredFunds1 = Double.parseDouble(requestRequiredFunds1);
            }
            if (!requestLifeEventAge2.equals("") && !requestRequiredFunds2.equals("")) {
                lifeEventAge2 = Integer.parseInt(requestLifeEventAge2);
                requiredFunds2 = Double.parseDouble(requestRequiredFunds2);
            }
            if (!requestLifeEventAge3.equals("") && !requestRequiredFunds3.equals("")) {
                lifeEventAge3 = Integer.parseInt(requestLifeEventAge3);
                requiredFunds3 = Double.parseDouble(requestRequiredFunds3);
            }
            if (!requestLifeEventAge4.equals("") && !requestRequiredFunds4.equals("")) {
                lifeEventAge4 = Integer.parseInt(requestLifeEventAge4);
                requiredFunds4 = Double.parseDouble(requestRequiredFunds4);
            }
            if (!requestLifeEventAge5.equals("") && !requestRequiredFunds5.equals("")) {
                lifeEventAge5 = Integer.parseInt(requestLifeEventAge5);
                requiredFunds5 = Double.parseDouble(requestRequiredFunds5);
            }

            // 詳細設定　→
            if (!annualChangePeriod.equals("")) {
                annualChangeMonth = getAnnualChangeMonth(annualChangePeriod);
                annualChangeMoney = Integer.parseInt(requestAnnualChangeMoney);
            }
            if (!requestEndingAge.equals("")) {
                endingAge = Integer.parseInt(requestEndingAge);
            }

            // 値のセット
            lifeEventParams = new lifeEventParams(lifeEvent1, lifeEventAge1, requiredFunds1, lifeEvent2, lifeEventAge2, requiredFunds2, lifeEvent3, lifeEventAge3, requiredFunds3, lifeEvent4, lifeEventAge4, requiredFunds4, lifeEvent5, lifeEventAge5, requiredFunds5);
            advancedSetting = new advancedSetting(annualChangeMonth, annualChangeMoney, endingAge);
            params = new SimulationParams(id, expectedRateOfReturn, volatility, startAge, monthlySavings, initialValue, lifeEventParams, advancedSetting);

            // バリデーションの初期化
            lifeEventValidMessage = new lifeEventValidation();
            advancedSettingValidMessage = new advancedSettingValidation();
            validMessage = new Validation();
            validateFlg = false;

            return "redirect:/list";
        } catch (Exception e) {
            // requestParamのセットとバリデーションの初期化
            lifeEventStr lifeEventStr = new lifeEventStr(requestLifeEventAge1, requestRequiredFunds1, requestLifeEventAge2, requestRequiredFunds2, requestLifeEventAge3, requestRequiredFunds3, requestLifeEventAge4, requestRequiredFunds4, requestLifeEventAge5, requestRequiredFunds5);
            lifeEventValidMessage = new lifeEventValidation();
            advancedSettingStr advancedSettingStr = new advancedSettingStr(requestAnnualChangeMoney, requestEndingAge);
            advancedSettingValidMessage = new advancedSettingValidation();
            validMessage = new Validation();

            // エラー文言のセット
            validMessage.typeValid(requestExpectedRateOfReturn, requestVolatility, requestStartAge, requestMonthlySavings, requestInitialValue, lifeEventStr, lifeEventValidMessage, advancedSettingStr, advancedSettingValidMessage);
            validateFlg = true;

            return "redirect:/list";
        }
    }
