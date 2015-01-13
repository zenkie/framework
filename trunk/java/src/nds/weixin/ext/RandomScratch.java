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
	 *            �ID
	 * @param wxVipId
	 *            ��ԱID
	 * @return winningLevel ��ƷID(-1û���н���-2�û�Ա�齱��������)��prize
	 *         ��Ʒ��Ϣ��wx_scratchticket_note_id �н���¼ID
	 * @throws JSONException
	 * @throws NumberFormatException
	 * @throws QueryException
	 */
	public synchronized String random(String adClientId,
			String wxScratchticketId, String wxVipId)
			throws NumberFormatException, JSONException, QueryException {
		// ��ѯÿ���������齱����
		String queryMAXTIMES = "select t.MAXTIMES from WX_SCRATCHTICKET t where t.ad_client_id = ? and t.id = ?";
		int maxtimes = Integer.parseInt(QueryEngine
				.getInstance()
				.doQueryOne(queryMAXTIMES.toString(),
						new Object[] { adClientId, wxScratchticketId })
				.toString());

		// ��ѯvip�Ѿ��齱����
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
		 * ��ѯ����������Ϣ rewarddegree:����ȼ� rewardtype:�������� giveouttype:������ʽ
		 * wx_coupon_id:�Ż�ȯ integral:���� rewardname:�������� rewardcount:����
		 * rewardrate:�н�����
		 */
		StringBuffer queryAward = new StringBuffer();
		queryAward
				.append("select t.id,t.rewarddegree,t.rewardtype,t.giveouttype,t.wx_coupon_id,t.integral,t.rewardname,t.rewardcount,t.rewardrate from WX_SCRATCHREWARD t ");
		/*
		 * ��ӹ������� ad_client_id:��˾id wx_scratchticket_id:�id
		 */
		queryAward
				.append("where t.ad_client_id = ? and t.wx_scratchticket_id = ? and ");
		/*
		 * ���һ������ ��Ʒ���͵ĸ�����������Ʒ���и���
		 */
		queryAward
				.append("(select count(*) from WX_SCRATCHTICKET_NOTE w where w.ad_client_id = ? and w.wx_scratchticket_id = ? and w.wx_scratchreward_id = t.id) < t.rewardcount");

		JSONArray array = QueryEngine.getInstance().doQueryObjectArray(
				queryAward.toString(),
				new Object[] { adClientId, wxScratchticketId, adClientId,
						wxScratchticketId });

		int winningLevel = -1;// �н��ȼ���δ�н�

		int noteID = QueryEngine.getInstance().getSequence(
				"WX_SCRATCHTICKET_NOTE");

		StringBuffer insertSQL = new StringBuffer();
		insertSQL
				.append("insert into WX_SCRATCHTICKET_NOTE(id,ad_client_id,ad_org_id,wx_scratchticket_id,wx_vip_id,receivetime,wx_scratchreward_id,ownerid,modifierid,creationdate,modifieddate,isactive) ");
		insertSQL
				.append("select ?, sn.id,sn.ad_org_id, ?,?,sysdate,?,sn.ownerid, sn.modifierid, sysdate, sysdate, 'Y' from ad_client sn where sn.id = ?");

		if (array == null || array.length() <= 0) {
			// �����н���¼
			QueryEngine.getInstance().executeUpdate(
					insertSQL.toString(),
					new Object[] { noteID, wxScratchticketId, wxVipId, "",
							adClientId });
			return "{\"winningLevel\":" + winningLevel
					+ ",\"prize\":\"\",\"wx_scratchticket_note_id\":" + noteID
					+ "}";
		}

		// �н������
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
				// �����н���¼
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
		// �����н���¼
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
	 *            �ID
	 * @param wxVipId
	 *            ��ԱID
	 * @param coupon
	 *            �Ż�ȯ��
	 * @return
	 * @throws QueryException
	 */
	public synchronized String assign(String adClientId,
			String wxScratchticketId, String wxVipId, String coupon)
			throws QueryException {
		// ��ѯÿ���������齱����
		String queryMAXTIMES = "select t.MAXTIMES from WX_SCRATCHTICKET t where t.ad_client_id = ? and t.id = ?";
		int maxtimes = Integer.parseInt(QueryEngine
				.getInstance()
				.doQueryOne(queryMAXTIMES.toString(),
						new Object[] { adClientId, wxScratchticketId })
				.toString());

		// ��ѯvip�Ѿ��齱����
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
			return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"��Ĺν������Ѿ�������\"}";
		}
		String queryCoupon = "select * from WX_SCRATCHTICKET_NOTE t where t.ad_client_id = ? and t.wx_scratchticket_id = ? and t.couponno = ?";
		JSONObject ticket = QueryEngine.getInstance().doQueryObject(
				queryCoupon,
				new Object[] { adClientId, wxScratchticketId, coupon });
		if (ticket == null)
			return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"�����ڸÄ�\"}";

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

			// return "��ȷ�Ą���";
			return "{\"winningLevel\":"
					+ ticket.optString("WX_SCRATCHREWARD_ID") + ",\"prize\":"
					+ prize + ",\"wx_scratchticket_note_id\":" + id
					+ ",\"message\":\"��ȷ�Ą���\"}";
		} else {
			if (ticket.optString("WX_VIP_ID").equals(wxVipId)) {
				return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"�Ä����Ѿ�����ʹ�ù������ѯ�н���¼��\"}";
			} else {
				return "{\"winningLevel\":-1,\"prize\":\"\",\"wx_scratchticket_note_id\":\"\",\"message\":\"�Ä����Ѿ���������Ա��ȡ\"}";
			}
		}
	}
}
