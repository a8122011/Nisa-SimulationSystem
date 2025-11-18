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
