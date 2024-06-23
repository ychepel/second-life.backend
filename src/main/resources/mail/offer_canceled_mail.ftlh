<#include "master_mail.ftlh">

<#macro title>Offer was canceled by owner!</#macro>
<#macro greeting>Dear, ${name}!</#macro>
<#macro maintext>Please, pay attention, that the offer was canceled by owner! <a href="${link}">offer</a>. You can search another offer on our marketplace with this  <a href="${active-offers}">link</a></#macro>

<@content/>
