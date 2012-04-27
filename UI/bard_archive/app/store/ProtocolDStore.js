/*
 * File: app/store/ProtocolDStore.js
 * Date: Tue Apr 24 2012 13:10:25 GMT-0400 (Eastern Daylight Time)
 *
 * This file was generated by Sencha Architect version 2.0.0.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 4.0.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 4.0.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.define('BARD.store.ProtocolDStore', {
    extend: 'Ext.data.Store',
    requires: [
        'BARD.model.Protocol'
    ],

    constructor: function(cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            storeId: 'MyJsonStore',
            model: 'BARD.model.Protocol',
            proxy: {
                type: 'ajax',
                url: 'api/protocol',
                reader: {
                    type: 'json'
                }
            }
        }, cfg)]);
    }
});