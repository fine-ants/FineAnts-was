UPDATE member m
    JOIN notification_preference np ON np.member_id = m.id
SET m.browser_notify     = np.browser_notify,
    m.max_loss_notify    = np.max_loss_notify,
    m.target_gain_notify = np.target_gain_notify,
    m.target_price_notify= np.target_price_notify;
