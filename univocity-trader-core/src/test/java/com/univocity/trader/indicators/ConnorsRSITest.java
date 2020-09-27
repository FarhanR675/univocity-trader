package com.univocity.trader.indicators;


import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static junit.framework.TestCase.*;

public class ConnorsRSITest {

	private double accumulateAndGet(ConnorsRSI b, int i, double price) {
		b.accumulate(newCandle(i, price));
		return b.getValue();
	}

	@Test
	public void isUsable() {
		ConnorsRSI b = new ConnorsRSI(TimeInterval.minutes(1));

		assertEquals(0.0, accumulateAndGet(b, 1, 0.03908), 0.001);
		assertEquals(0.0, accumulateAndGet(b, 2, 0.03908), 0.001);
		assertEquals(83.33333333333333, accumulateAndGet(b, 3, 0.03916), 0.001);
		assertEquals(39.18128654970645, accumulateAndGet(b, 4, 0.03915), 0.001);
		assertEquals(25.71428571428572, accumulateAndGet(b, 5, 0.03913), 0.001);
		assertEquals(73.94548063127691, accumulateAndGet(b, 6, 0.03921), 0.001);
		assertEquals(30.724778415636212, accumulateAndGet(b, 7, 0.03916), 0.001);
		assertEquals(26.700262707678622, accumulateAndGet(b, 8, 0.03913), 0.001);
		assertEquals(52.4753515042592, accumulateAndGet(b, 9, 0.03913), 0.001);
		assertEquals(65.12740166342006, accumulateAndGet(b, 10, 0.03914), 0.001);
		assertEquals(79.75171364383154, accumulateAndGet(b, 11, 0.03921), 0.001);
		assertEquals(38.73565550333941, accumulateAndGet(b, 12, 0.03919), 0.001);
		assertEquals(51.60181878993128, accumulateAndGet(b, 13, 0.03919), 0.001);
		assertEquals(74.9608828576661, accumulateAndGet(b, 14, 0.03926), 0.001);
		assertEquals(75.9820791659548, accumulateAndGet(b, 15, 0.03927), 0.001);
		assertEquals(83.2306682587996, accumulateAndGet(b, 16, 0.0393), 0.001);
		assertEquals(49.10362025459302, accumulateAndGet(b, 17, 0.0393), 0.001);
		assertEquals(26.507508471096546, accumulateAndGet(b, 18, 0.03927), 0.001);
		assertEquals(70.9926489063053, accumulateAndGet(b, 19, 0.03932), 0.001);
		assertEquals(32.65800178067352, accumulateAndGet(b, 20, 0.03929), 0.001);
		assertEquals(64.97081743979281, accumulateAndGet(b, 21, 0.03931), 0.001);
		assertEquals(76.69797284785055, accumulateAndGet(b, 22, 0.03936), 0.001);
		assertEquals(77.01295766869644, accumulateAndGet(b, 23, 0.03938), 0.001);
		assertEquals(41.32861402698139, accumulateAndGet(b, 24, 0.03937), 0.001);
		assertEquals(11.75353092858667, accumulateAndGet(b, 25, 0.03925), 0.001);
		assertEquals(64.36726065441518, accumulateAndGet(b, 26, 0.0393), 0.001);
		assertEquals(21.835642661269137, accumulateAndGet(b, 27, 0.03921), 0.001);
		assertEquals(14.198504875643836, accumulateAndGet(b, 28, 0.0391), 0.001);
		assertEquals(11.914930201202004, accumulateAndGet(b, 29, 0.03903), 0.001);
		assertEquals(72.06307009905441, accumulateAndGet(b, 30, 0.03911), 0.001);
		assertEquals(29.690990682757903, accumulateAndGet(b, 31, 0.03906), 0.001);
		assertEquals(25.342000738283733, accumulateAndGet(b, 32, 0.03903), 0.001);
		assertEquals(56.44648241227534, accumulateAndGet(b, 33, 0.03904), 0.001);
		assertEquals(32.01563944402235, accumulateAndGet(b, 34, 0.03902), 0.001);
		assertEquals(56.2033573209082, accumulateAndGet(b, 35, 0.03903), 0.001);
		assertEquals(23.997291150423788, accumulateAndGet(b, 36, 0.03899), 0.001);
		assertEquals(54.953989424498275, accumulateAndGet(b, 37, 0.039), 0.001);
		assertEquals(39.7183596145465, accumulateAndGet(b, 38, 0.039), 0.001);
		assertEquals(18.835206479072806, accumulateAndGet(b, 39, 0.03896), 0.001);
		assertEquals(61.79354986200542, accumulateAndGet(b, 40, 0.03898), 0.001);
		assertEquals(74.50657438040902, accumulateAndGet(b, 41, 0.03901), 0.001);
		assertEquals(33.25176958401179, accumulateAndGet(b, 42, 0.03899), 0.001);
		assertEquals(68.0205121102821, accumulateAndGet(b, 43, 0.03902), 0.001);
		assertEquals(73.20397376269928, accumulateAndGet(b, 44, 0.03904), 0.001);
		assertEquals(49.687102259158536, accumulateAndGet(b, 45, 0.03904), 0.001);
		assertEquals(25.77145500126599, accumulateAndGet(b, 46, 0.03901), 0.001);
		assertEquals(26.751615212344557, accumulateAndGet(b, 47, 0.039), 0.001);
		assertEquals(68.0205950396785, accumulateAndGet(b, 48, 0.03902), 0.001);
		assertEquals(49.72763097185007, accumulateAndGet(b, 49, 0.03902), 0.001);
		assertEquals(17.152084594249178, accumulateAndGet(b, 50, 0.03895), 0.001);
		assertEquals(39.250823285298786, accumulateAndGet(b, 51, 0.03895), 0.001);
		assertEquals(38.97631348137721, accumulateAndGet(b, 52, 0.03895), 0.001);
		assertEquals(38.712361746837246, accumulateAndGet(b, 53, 0.03895), 0.001);
		assertEquals(6.603732357527683, accumulateAndGet(b, 54, 0.03886), 0.001);
		assertEquals(5.92294638681991, accumulateAndGet(b, 55, 0.0388), 0.001);
		assertEquals(0.9883390486543154, accumulateAndGet(b, 56, 0.03868), 0.001);
		assertEquals(57.8554520003867, accumulateAndGet(b, 57, 0.0387), 0.001);
		assertEquals(19.044167299745585, accumulateAndGet(b, 58, 0.0386), 0.001);
		assertEquals(56.33214158043112, accumulateAndGet(b, 59, 0.03862), 0.001);
		assertEquals(16.330999567406753, accumulateAndGet(b, 60, 0.03851), 0.001);
		assertEquals(59.21348357262135, accumulateAndGet(b, 61, 0.03854), 0.001);
		assertEquals(75.02696966656912, accumulateAndGet(b, 62, 0.03861), 0.001);
		assertEquals(74.17811705902403, accumulateAndGet(b, 63, 0.03863), 0.001);
		assertEquals(78.01429141364754, accumulateAndGet(b, 64, 0.03865), 0.001);
		assertEquals(84.99269930679948, accumulateAndGet(b, 65, 0.03868), 0.001);
		assertEquals(80.01537650714032, accumulateAndGet(b, 66, 0.03869), 0.001);
		assertEquals(28.220061771716036, accumulateAndGet(b, 67, 0.03866), 0.001);
		assertEquals(63.89966080948951, accumulateAndGet(b, 68, 0.03869), 0.001);
		assertEquals(46.3076506941782, accumulateAndGet(b, 69, 0.03869), 0.001);
		assertEquals(63.683682300556825, accumulateAndGet(b, 70, 0.0387), 0.001);
		assertEquals(32.602864790571815, accumulateAndGet(b, 71, 0.03868), 0.001);
		assertEquals(28.579042920135958, accumulateAndGet(b, 72, 0.03867), 0.001);
		assertEquals(47.098823223266514, accumulateAndGet(b, 73, 0.03867), 0.001);
		assertEquals(33.010061583488145, accumulateAndGet(b, 74, 0.03866), 0.001);
		assertEquals(17.307127879883605, accumulateAndGet(b, 75, 0.03863), 0.001);
		assertEquals(9.937317568614198, accumulateAndGet(b, 76, 0.03858), 0.001);
		assertEquals(68.14099224599292, accumulateAndGet(b, 77, 0.03861), 0.001);
		assertEquals(29.87301735470886, accumulateAndGet(b, 78, 0.03858), 0.001);
		assertEquals(71.19642296327454, accumulateAndGet(b, 79, 0.03862), 0.001);
		assertEquals(17.1440996946928, accumulateAndGet(b, 80, 0.03844), 0.001);
		assertEquals(18.025533260578854, accumulateAndGet(b, 81, 0.0384), 0.001);
		assertEquals(62.92047587590664, accumulateAndGet(b, 82, 0.03843), 0.001);
		assertEquals(79.7461953627974, accumulateAndGet(b, 83, 0.03851), 0.001);
		assertEquals(36.454733657227116, accumulateAndGet(b, 84, 0.03849), 0.001);
		assertEquals(77.3738540097778, accumulateAndGet(b, 85, 0.03857), 0.001);
		assertEquals(79.78493963425956, accumulateAndGet(b, 86, 0.03861), 0.001);
		assertEquals(51.333407732777495, accumulateAndGet(b, 87, 0.03861), 0.001);
		assertEquals(71.07689784200852, accumulateAndGet(b, 88, 0.03863), 0.001);
		assertEquals(52.67148072715321, accumulateAndGet(b, 89, 0.03863), 0.001);
		assertEquals(82.09959177200479, accumulateAndGet(b, 90, 0.03868), 0.001);
		assertEquals(79.00848984986503, accumulateAndGet(b, 91, 0.03869), 0.001);
		assertEquals(81.87814928478339, accumulateAndGet(b, 92, 0.0387), 0.001);
		assertEquals(52.35741854563191, accumulateAndGet(b, 93, 0.0387), 0.001);
		assertEquals(20.033120364218988, accumulateAndGet(b, 94, 0.03865), 0.001);
		assertEquals(57.23599023005187, accumulateAndGet(b, 95, 0.03866), 0.001);
		assertEquals(40.88263528215726, accumulateAndGet(b, 96, 0.03866), 0.001);
		assertEquals(75.72012886873857, accumulateAndGet(b, 97, 0.0387), 0.001);
		assertEquals(87.44056637170628, accumulateAndGet(b, 98, 0.03876), 0.001);
		assertEquals(23.974141801325413, accumulateAndGet(b, 99, 0.03868), 0.001);
		assertEquals(25.964417506882317, accumulateAndGet(b, 100, 0.03866), 0.001);
		assertEquals(63.42247146061144, accumulateAndGet(b, 101, 0.03868), 0.001);
		assertEquals(26.078161969313864, accumulateAndGet(b, 102, 0.03863), 0.001);
		assertEquals(28.34408448959589, accumulateAndGet(b, 103, 0.03862), 0.001);
		assertEquals(43.89767977510133, accumulateAndGet(b, 104, 0.03862), 0.001);
		assertEquals(20.288706604300486, accumulateAndGet(b, 105, 0.03855), 0.001);
		assertEquals(14.42288203911952, accumulateAndGet(b, 106, 0.0385), 0.001);
		assertEquals(53.381825051689965, accumulateAndGet(b, 107, 0.03851), 0.001);
		assertEquals(23.886463874943313, accumulateAndGet(b, 108, 0.03848), 0.001);
		assertEquals(13.182554714010337, accumulateAndGet(b, 109, 0.0384), 0.001);
		assertEquals(37.97915962450072, accumulateAndGet(b, 110, 0.0384), 0.001);
		assertEquals(18.59585232332802, accumulateAndGet(b, 111, 0.03833), 0.001);
		assertEquals(19.352199260370014, accumulateAndGet(b, 112, 0.03831), 0.001);
		assertEquals(17.427809494832445, accumulateAndGet(b, 113, 0.0383), 0.001);
		assertEquals(4.590201897643747, accumulateAndGet(b, 114, 0.03821), 0.001);
		assertEquals(6.3453536420110765, accumulateAndGet(b, 115, 0.03815), 0.001);
		assertEquals(67.97420355681969, accumulateAndGet(b, 116, 0.03819), 0.001);
		assertEquals(32.17420209163425, accumulateAndGet(b, 117, 0.03815), 0.001);
		assertEquals(43.854979234395465, accumulateAndGet(b, 118, 0.03815), 0.001);
		assertEquals(77.6785308096221, accumulateAndGet(b, 119, 0.03821), 0.001);
		assertEquals(41.289423111113834, accumulateAndGet(b, 120, 0.0382), 0.001);
		assertEquals(25.210503316163244, accumulateAndGet(b, 121, 0.03816), 0.001);
		assertEquals(82.84665880857021, accumulateAndGet(b, 122, 0.03827), 0.001);
		assertEquals(36.134823861839756, accumulateAndGet(b, 123, 0.03822), 0.001);
		assertEquals(72.40765770050623, accumulateAndGet(b, 124, 0.03825), 0.001);
		assertEquals(39.753805891659916, accumulateAndGet(b, 125, 0.03823), 0.001);
		assertEquals(77.67502929022989, accumulateAndGet(b, 126, 0.03829), 0.001);
		assertEquals(46.42823215780413, accumulateAndGet(b, 127, 0.03828), 0.001);
		assertEquals(19.48508256725691, accumulateAndGet(b, 128, 0.0382), 0.001);
		assertEquals(14.701339543837653, accumulateAndGet(b, 129, 0.03814), 0.001);
		assertEquals(63.32003040765985, accumulateAndGet(b, 130, 0.03816), 0.001);
		assertEquals(28.807067140553006, accumulateAndGet(b, 131, 0.03812), 0.001);
		assertEquals(69.5303216779155, accumulateAndGet(b, 132, 0.03816), 0.001);
		assertEquals(26.924815672050737, accumulateAndGet(b, 133, 0.03811), 0.001);
		assertEquals(15.323455203813362, accumulateAndGet(b, 134, 0.03803), 0.001);
		assertEquals(64.55208259661843, accumulateAndGet(b, 135, 0.03806), 0.001);
		assertEquals(76.85833708752462, accumulateAndGet(b, 136, 0.03811), 0.001);
		assertEquals(32.984316259280924, accumulateAndGet(b, 137, 0.03808), 0.001);
		assertEquals(58.962865108241544, accumulateAndGet(b, 138, 0.03809), 0.001);
		assertEquals(37.464233193139414, accumulateAndGet(b, 139, 0.03808), 0.001);
		assertEquals(31.580208110190956, accumulateAndGet(b, 140, 0.03807), 0.001);
		assertEquals(63.244333318714325, accumulateAndGet(b, 141, 0.03808), 0.001);
		assertEquals(69.32252007209631, accumulateAndGet(b, 142, 0.03809), 0.001);
		assertEquals(47.696374392209975, accumulateAndGet(b, 143, 0.03809), 0.001);
		assertEquals(21.699553518619414, accumulateAndGet(b, 144, 0.03805), 0.001);
		assertEquals(6.367105767480136, accumulateAndGet(b, 145, 0.03791), 0.001);
		assertEquals(52.32348002848122, accumulateAndGet(b, 146, 0.03792), 0.001);
		assertEquals(73.10706234237144, accumulateAndGet(b, 147, 0.03797), 0.001);
		assertEquals(88.23770619221379, accumulateAndGet(b, 148, 0.03809), 0.001);
		assertEquals(83.06176143573454, accumulateAndGet(b, 149, 0.03811), 0.001);
		assertEquals(38.64999505367564, accumulateAndGet(b, 150, 0.03809), 0.001);
		assertEquals(72.65143935875159, accumulateAndGet(b, 151, 0.03814), 0.001);
		assertEquals(53.59885547171373, accumulateAndGet(b, 152, 0.03814), 0.001);
		assertEquals(43.511906133607134, accumulateAndGet(b, 153, 0.03813), 0.001);
		assertEquals(68.2739804623609, accumulateAndGet(b, 154, 0.03814), 0.001);
		assertEquals(73.70598369400709, accumulateAndGet(b, 155, 0.03815), 0.001);
		assertEquals(77.86737547234019, accumulateAndGet(b, 156, 0.03816), 0.001);
		assertEquals(12.642101588619482, accumulateAndGet(b, 157, 0.03804), 0.001);
		assertEquals(8.922818918405591, accumulateAndGet(b, 158, 0.03795), 0.001);
		assertEquals(54.62475264196826, accumulateAndGet(b, 159, 0.03797), 0.001);
		assertEquals(16.967735980521493, accumulateAndGet(b, 160, 0.03788), 0.001);
		assertEquals(51.165736460168, accumulateAndGet(b, 161, 0.03789), 0.001);
		assertEquals(62.386684142767564, accumulateAndGet(b, 162, 0.03791), 0.001);
		assertEquals(73.84552506719568, accumulateAndGet(b, 163, 0.03794), 0.001);
		assertEquals(87.26978187944944, accumulateAndGet(b, 164, 0.038), 0.001);
		assertEquals(93.71076732291515, accumulateAndGet(b, 165, 0.0381), 0.001);
		assertEquals(94.78510999865557, accumulateAndGet(b, 166, 0.03817), 0.001);
		assertEquals(40.888199657633656, accumulateAndGet(b, 167, 0.03815), 0.001);
		assertEquals(51.69458524631, accumulateAndGet(b, 168, 0.03815), 0.001);
		assertEquals(64.54657962167735, accumulateAndGet(b, 169, 0.03816), 0.001);
		assertEquals(26.35083875892442, accumulateAndGet(b, 170, 0.03811), 0.001);
		assertEquals(70.44387248092704, accumulateAndGet(b, 171, 0.03815), 0.001);
		assertEquals(41.87110936505686, accumulateAndGet(b, 172, 0.03814), 0.001);
		assertEquals(51.62223593244628, accumulateAndGet(b, 173, 0.03814), 0.001);
		assertEquals(51.28890259911295, accumulateAndGet(b, 174, 0.03814), 0.001);
		assertEquals(27.84715377233761, accumulateAndGet(b, 175, 0.03812), 0.001);
		assertEquals(45.880494713590394, accumulateAndGet(b, 176, 0.03812), 0.001);
		assertEquals(23.02298947412668, accumulateAndGet(b, 177, 0.03809), 0.001);
		assertEquals(64.8966206056957, accumulateAndGet(b, 178, 0.03811), 0.001);
		assertEquals(74.51307755220502, accumulateAndGet(b, 179, 0.03813), 0.001);
		assertEquals(37.49096243004581, accumulateAndGet(b, 180, 0.03812), 0.001);
		assertEquals(47.46455324591028, accumulateAndGet(b, 181, 0.03812), 0.001);
		assertEquals(75.6792067213628, accumulateAndGet(b, 182, 0.03815), 0.001);
		assertEquals(80.34465712560375, accumulateAndGet(b, 183, 0.03817), 0.001);
		assertEquals(95.6738481785028, accumulateAndGet(b, 184, 0.0383), 0.001);
		assertEquals(21.624087699813984, accumulateAndGet(b, 185, 0.03817), 0.001);
		assertEquals(23.84295640967829, accumulateAndGet(b, 186, 0.03813), 0.001);
		assertEquals(61.72376481090339, accumulateAndGet(b, 187, 0.03815), 0.001);
		assertEquals(45.023312854994735, accumulateAndGet(b, 188, 0.03815), 0.001);
		assertEquals(35.54280411375398, accumulateAndGet(b, 189, 0.03814), 0.001);
		assertEquals(47.47254758423437, accumulateAndGet(b, 190, 0.03814), 0.001);
		assertEquals(28.596786908802105, accumulateAndGet(b, 191, 0.03812), 0.001);
		assertEquals(20.546597984930987, accumulateAndGet(b, 192, 0.0381), 0.001);
		assertEquals(59.00056196737547, accumulateAndGet(b, 193, 0.03811), 0.001);
		assertEquals(33.978269133283234, accumulateAndGet(b, 194, 0.0381), 0.001);
		assertEquals(85.16779679168708, accumulateAndGet(b, 195, 0.03823), 0.001); /////// ADAUSDT, 2019-SEP-26 12:48, 1-minute chart
		assertEquals(84.40732640917844, accumulateAndGet(b, 196, 0.03827), 0.001);
		assertEquals(50.53844538476611, accumulateAndGet(b, 197, 0.03826), 0.001);
		assertEquals(75.82436407645655, accumulateAndGet(b, 198, 0.03829), 0.001);
		assertEquals(36.44394380985511, accumulateAndGet(b, 199, 0.03825), 0.001);
		assertEquals(61.52786357643115, accumulateAndGet(b, 200, 0.03826), 0.001);
	}
}

