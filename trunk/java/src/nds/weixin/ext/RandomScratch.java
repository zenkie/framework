package nds.weixin.ext;

import java.util.Hashtable;

import nds.query.QueryEngine;
import nds.query.QueryException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RandomScratch {
	// private final static Log log = LogFactory.getLog(RandomScratch.class);
	private static Hashtable<String, RandomScratch> factorys;
	private String adClientId;

	private RandomScratch() {
	};

	public static synchronized RandomScratch getInstance(String adClientId) {
		if (adClientId == null)
			return null;

		RandomScratch instance = null;
		if (factorys == null) {
			factorys = new Hashtable<String, RandomScratch>();
			instance = new RandomScratch();
			factorys.put(adClientId, instance);
		} else if (factorys.containsKey(adClientId)) {
			instance = factorys.get(adClientId);
		} else {
			instance = new RandomScratch();
			factorys.put(adClientId, instance);
		}
		instance.adClientId = adClientId;
		return instance;
	}

	/**
	 * 
	 * @param wxScratchticketId
	 *            活动ID
	 * @param wxVipId
	 *            会员ID
	 * @return winningLevel 奖品ID(-1没有中奖，-2该会员抽奖次数用完)，prize
	 *         奖品信息，wx_scratchticket_note_id 中奖纪录ID
	 * @throws JSONException
	 * @throws NumberFormatException
	 * @throws QueryException
	 */
	public synchronized String random(String adClientId,
			String wxScratchticketId, String wxVipId)
			throws NumberFormatException, JSONException, QueryException {
		// 查询每人最多允许抽奖次数
		String queryMAXTIMES = "select t.MAXTIMES from WX_SCRATCHTICKET t where t.ad_client_id = ? and t.id = ?";
		int maxtimes = Integer.parseInt(QueryEngine
				.getInstance()
				.doQueryOne(queryMAXTIMES.toString(),
						new Object[] { adClientId, wxScratchticketId })
				.toString());

		// 查询vip已经抽奖次数
		String queryUse = "select count(*) from WX_SCRATCHTICKET_NOTE t where t.ad_client_id = ? and t.wx_scratchticket_id = ? and t.wx_vip_id = ?";
		int useCount = Integer
				.parseInt(QueryEngine
						.getInstance()
						.doQueryOne(
								queryUse.toString(),
								new Object[] { adClientId, wxScratchticketId,
										wxVipId }).toString());

		int remainCount = maxtimes - useCount;
		if (remainCount <= 0) {
			return "{\"winningLevel\":-2,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\"}";
		}
		/*
		 * 查询奖项设置信息 rewarddegree:奖项等级 rewardtype:奖项类型 giveouttype:发放形式
		 * wx_coupon_id:优惠券 integral:积分 rewardname:奖项名称 rewardcount:个数
		 * rewardrate:中奖概率
		 */
		StringBuffer queryAward = new StringBuffer();
		queryAward
				.append("select t.id,t.rewarddegree,t.rewardtype,t.giveouttype,t.wx_coupon_id,t.integral,t.rewardname,t.rewardcount,t.rewardrate from WX_SCRATCHREWARD t ");
		/*
		 * 添加过滤条件 ad_client_id:公司id wx_scratchticket_id:活动id
		 */
		queryAward
				.append("where t.ad_client_id = ? and t.wx_scratchticket_id = ? and ");
		/*
		 * 最后一个条件 奖品发送的个数不超过奖品已有个数
		 */
		queryAward
				.append("(select count(*) from WX_SCRATCHTICKET_NOTE w where w.ad_client_id = ? and w.wx_scratchticket_id = ? and w.wx_scratchreward_id = t.id) < t.rewardcount");

		JSONArray array = QueryEngine.getInstance().doQueryObjectArray(
				queryAward.toString(),
				new Object[] { adClientId, wxScratchticketId, adClientId,
						wxScratchticketId });

		int winningLevel = -1;// 中奖等级：未中奖

		int noteID = QueryEngine.getInstance().getSequence(
				"WX_SCRATCHTICKET_NOTE");

		StringBuffer insertSQL = new StringBuffer();
		insertSQL
				.append("insert into WX_SCRATCHTICKET_NOTE(id,ad_client_id,ad_org_id,wx_scratchticket_id,wx_vip_id,receivetime,wx_scratchreward_id,ownerid,modifierid,creationdate,modifieddate,isactive) ");
		insertSQL
				.append("select ?, sn.id,sn.ad_org_id, ?,?,sysdate,?,sn.ownerid, sn.modifierid, sysdate, sysdate, 'Y' from ad_client sn where sn.id = ?");

		if (array == null || array.length() <= 0) {
			// 插入中奖纪录
			QueryEngine.getInstance().executeUpdate(
					insertSQL.toString(),
					new Object[] { noteID, wxScratchticketId, wxVipId, "",
							adClientId });
			return "{\"winningLevel\":" + winningLevel
					+ ",\"prize\":\"\",\"wx_scratchticket_note_id\":" + noteID
					+ "}";
		}

		// 中奖随机号
		int randomWinningNo = 0;
		int args[] = new int[array.length() * 2];
		int temp = (int) Math.round(Math.random() * 1000000000) % 1000000;
		int j = 0;

		for (int i = 0, length = array.length(); i < length; i++) {

			double tmpWinningPro = Double.parseDouble(array.getJSONObject(i)
					.getString("REWARDRATE"));

			if (j == 0) {
				args[j] = randomWinningNo;
			} else {
				args[j] = args[j - 1] + 1;
			}
			args[j + 1] = args[j] + (int) Math.round(tmpWinningPro * 10000) - 1;

			if (temp >= args[j] && temp <= args[j + 1]) {
				// 插入中奖纪录
				QueryEngine.getInstance().executeUpdate(
						insertSQL.toString(),
						new Object[] { noteID, wxScratchticketId, wxVipId,
								array.getJSONObject(i).getString("ID"),
								adClientId });
				return "{\"winningLevel\":"
						+ array.getJSONObject(i).getString("ID")
						+ ",\"prize\":" + array.getJSONObject(i)
						+ ",\"wx_scratchticket_note_id\":" + noteID + "}";
			}
			j += 2;
		}
		// 插入中奖纪录
		QueryEngine.getInstance().executeUpdate(
				insertSQL.toString(),
				new Object[] { noteID, wxScratchticketId, wxVipId, "",
						adClientId });
		return "{\"winningLevel\":" + winningLevel
				+ ",\"prize\":\"\",\"wx_scratchticket_note_id\":" + noteID
				+ "}";
	}

	/**
	 * 
	 * @param wxScratchticketId
	 *            活动ID
	 * @param wxVipId
	 *            会员ID
	 * @param coupon
	 *            优惠券号
	 * @return
	 * @throws QueryException
	 */
	public synchronized String assign(String adClientId,
			String wxScratchticketId, String wxVipId, String coupon)
			throws QueryException {
		// 查询每人最多允许抽奖次数
		String queryMAXTIMES = "select t.MAXTIMES from WX_SCRATCHTICKET t where t.ad_client_id = ? and t.id = ?";
		int maxtimes = Integer.parseInt(QueryEngine
				.getInstance()
				.doQueryOne(queryMAXTIMES.toString(),
						new Object[] { adClientId, wxScratchticketId })
				.toString());

		// 查询vip已经抽奖次数
		String queryUse = "select count(*) from WX_SCRATCHTICKET_NOTE t where t.ad_client_id = ? and t.wx_scratchticket_id = ? and t.wx_vip_id = ?";
		int useCount = Integer
				.parseInt(QueryEngine
						.getInstance()
						.doQueryOne(
								queryUse.toString(),
								new Object[] { adClientId, wxScratchticketId,
										wxVipId }).toString());

		int remainCount = maxtimes - useCount;
		if (remainCount <= 0) {
			return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"你的刮奖次数已经用完了\"}";
		}
		String queryCoupon = "select * from WX_SCRATCHTICKET_NOTE t where t.ad_client_id = ? and t.wx_scratchticket_id = ? and t.couponno = ?";
		JSONObject ticket = QueryEngine.getInstance().doQueryObject(
				queryCoupon,
				new Object[] { adClientId, wxScratchticketId, coupon });
		if (ticket == null)
			return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"不存在该劵\"}";

		if (ticket.optString("COUPONISUSED").equals("N")) {
			String id = ticket.optString("ID");
			String updateSQL = "update WX_SCRATCHTICKET_NOTE t set t.wx_vip_id = ?,t.couponisused='Y' where t.ad_client_id = ? and t.id = ?";
			QueryEngine.getInstance().executeUpdate(updateSQL,
					new Object[] { wxVipId, adClientId, id });
			StringBuffer queryAward = new StringBuffer();
			queryAward
					.append("select t.id,t.rewarddegree,t.rewardtype,t.giveouttype,t.wx_coupon_id,t.integral,t.rewardname,t.rewardcount,t.rewardrate from WX_SCRATCHREWARD t ");
			queryAward
					.append("where t.ad_client_id = ? and t.wx_scratchticket_id = ? and t.id = ?");

			JSONObject prize = QueryEngine.getInstance().doQueryObject(
					queryAward.toString(),
					new Object[] { adClientId, wxScratchticketId,
							ticket.optString("WX_SCRATCHREWARD_ID") });

			// return "正确的劵号";
			return "{\"winningLevel\":"
					+ ticket.optString("WX_SCRATCHREWARD_ID") + ",\"prize\":"
					+ prize + ",\"wx_scratchticket_note_id\":" + id
					+ ",\"message\":\"正确的劵号\"}";
		} else {
			if (ticket.optString("WX_VIP_ID").equals(wxVipId)) {
				return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"该劵号已经被您使用过，请查询中奖记录。\"}";
			} else {
				return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"该劵号已经被其他会员领取\"}";
			}
		}
	}
}
