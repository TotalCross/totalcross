/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.test.totalcross.ui.gfx;

import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.unit.ImageComparisionTest;
import totalcross.unit.ImageTester;

public class GraphicsTest extends ImageComparisionTest
{
  private Graphics g;

  private static int back = Color.YELLOW;

  private String setPixel_65536 = "58C385D3C116C0100C44D1F96C9F5F5534613263E9BE8543A0A905341897C1EB2A182E82CFEB607A192CAF82ED45F03B0F82D3203A0B929320FB1D1C7E05B8368C1F01C8918CA700F4528C8700C5B318DF01CAC1303E0388D1343E02C8CF61BC07DA3BE3013A6997E0";
  private String drawLine_65536 = "58C38D935B1280200C0373EC3DBE8EA3435F10FBBB29943448AED8D78375C437DD09C47BBC36F8E3A3402C3E0844E44D20322F0251791288CCCB9082DE1FBA9839ED2DD5FE6A565B4FD946F74F4C3C8C36F6FFBCDFCC6FDE6FFC3BF9EFF767F66FF263F267F26BF26FFE8FA90BB62F54D8";
  private String drawRect_65536 = "58C3636020042A700302D25493C7E50EB83C14D34C1E8FFD70092C410666C3E511DAB0EBC72E8F293A5CE57184DF20487F03294F000000B5C45280";
  private String fillRect_65536 = "58C37BF6AC020F6060784640BEA2820EF2CF406070CA23249E210015E511768D74F9014C7F83581E003FBCEA42";
  private String fillPie_65536 = "58C3D5924B0EC0200844E7D87362CE518592CA27BA6AD2CE06E10D60A2E04E187A9BE33FBCD86301C8869003C5B0A6403068C85844DC334E95BB41A63A1E742F79B8B5D970EF67B7DFC22C92AC0BB4D76A6C0CD273169C384F5C25E1D2DDA3AE0379F8FF5FE7176DB35F82";
  private String fillEllipticalPie_65536 = "58C3A593511280200805F7D89C9873540EA908659A1F0EB24F640045DE16C88433E3ACF0518E1790E83B078C02778620E88FC5D6246267ABEA2E37EC05F17AE5650FE9192F26AE5EB7575B20D73FE20ABCBE7FEED9FD153E8B1FE6CBA58F6C71EB5FCA5B052A96649AAE180F5C42D5C6E9B67EF0FDFFFCE5074E2F5668";
  private String fillPolygon_65536 = "58C3ABA800826715380003430511F2CF9E3DA344FED9339C0A1818A072389480E52B70DB418C3C9449AE3C9CFD0C77F8551008DF51F9517932E5019FEDB86A";
  private String drawRoundRect_65536 = "58C363A8A860C0012A4052405C81033080E5714A8314404CC1AD8058791CEA60F20C15D89D492D797CF633A070183084E0063320071C36FDD8E52B468C3C8EF0A346FA19387942F98360FE22903F01CACA5AF0";
  private String fillRoundRect_65536 = "58C3E5D3C10A00200803D07DF6BED8EFC820100FCEA0A0433BF6265E12A4152109D43C0BEE26E26E32BB8EA2B89E81A270C9C5FE004492A3713D9F77FDEC27FFE79D77F7D1DE57739F03E2659811";
  private String draw3dRect_65536 = "58C36360C0039E31E00537C0F21538C1C8927F06030C38E46F4000034E79083D64E5E1E902BB3C0301F9119A7E6EE00140F967780103030018FD9C98";
  private String drawCircle_65536 = "58C38D530112802008DBB3F7FC3A01194A9AD59DB836262208C6808D8CDF95092750663E179686C1C7A69F347820AAF307C3B1261841ACCEA0F8B1CFF54B829487E42FFB0EF6F05F2C62B2E195F02D8A094D1F78C2099A9E640B175081A063916EF0E65D707B58777DE673CDDFE1E21F5AF4CD7DA92E518BD3D78FE7FAE7BC3FBFEBF9F3D23FBCF41F2FFD7BEE7FF0CFFDF9BE7F0FEEC92850";
  private String fillCircle_65536 = "58C3AD93CB15C0200804297B2BDE3AF2310A8BA897703026333E0D82611776C77F9C2D1237A56238A746E66429744ED6C2C7C98590F8FB3A73C122083753A1F380FDD184B07C7C7721F3671CB392FB30380EEBB1DFDF93DBCF4F393F5498FE3F5E5E953F4C82E61FC0F6FEB01210F9BA7E500BC87C55BFB11BAAFAC7A1FF4EFC02D0D00E2D";
  private String drawDots_65536 = "58C38D93611A80200843776C4EBC73649A25082EFDE357BC090341D4BB2DD2EA754718CADF9D6F51153D782B14D005382233DA269F29E049805F74A4173E2AE02D809EF0E7C9C32B60318009E5FD8BB7C219C8ADE6E8BFCF1BA101F492DCFBB77E2EF89FF78BFC45FDC2BF93FFBA7FA2FF627EC4FC89F915F37F7E3FE071E3029C0BA050";
  private String drawDottedRect_65536 = "58C36378C6801B02C1B30A3C00A4029FFC88D2CF30C2FD3F30FAC1C99480FE6704ECA7443F38A3D0D0FF0CCFF042060025ABA370";
  private String setClip_65536 = "58C37BF60C0554A002068667A3F2A4C8A381819747B875547E38CA0300ED8DA7CF";
  private String translate_65536 = "58C3D5924B12C020084373EC1CBF53DB2A099F7D59E8C803F9924BF0084111E05C386FE3AF11B3C176DC5C23DC5C31C52064043D9C8335CF381A7C5C8173985EF80E71D4F97FD3563C665DE42F5DB1FAA15DD7FAE133B3FE71C032BF02CBFC0B2CFBC3767FC2FEB1E18DFC9C63E698FDD7E42F69217AD0";
  private String drawEllipse_65536 = "58C3CD934912C0200804E7D9F3FC94904244961CC3456B1A954D721BD40EC5C36B47A791C9395B190D9EDFF8D594675855E139167DF10A2FF281E38C38A4B979E689F97CFD7A28F55F79EB21F9A3BD1ED66A94F545175EDD42EB6FCB09B4F35371ECFC87F91DE77FFC3FF5FF7B00F0F67BC0";
  private String fillEllipse_65536 = "58C3DDD2410AC0200C44518F3D27FEE7705108C6D8A1E0A26276F22681102557ADE912C73BB0D51F53F29CEC300792430458380C81637C78557E1CB73F25A0EABCDF377C99FAD82FEFF2FEEBFFEE54DDE588";
  private String drawArc_65536 = "58C38D93010EC0200803FBEC3E7FD998836A279A18B58744B182D9104D1422E1324B2EBB7239B8D254EE3838FC6AC11D8E80877B1C897F928F80035ED3AFE5110E4E77558E698C7C22477A94F34370E98E7FB2E728130CCE735EFBC2F5FC58FCA3F747D687A67EA0E359FFC29DB7B07D5FE8FBB3F10F1BFFB1F16FE7FFE6FF70FFFF2E51566108";
  private String drawPie_65536 = "58C38D93010E80300803FBEC3EDF288E5299E21213C741C780815A88651642B0FD895B94B68B3B95E5F4C30EDFB6E03B1C0E17DFE3107E115F0E3F7895EFE531DE2EE31CE5939E0A9CA54556B99C170044A6D179E4BB6C8DE3B1BC9579044CFF913D5095E1B7A702DBFC64CB4B0D603C1D4AB8373FE7B3F0DEBF4CEAA5BFF0FE73981F0EF3C7617EA7F91FDE0FBFDFDF012A154FB0";
  private String drawEllipticalArc_65536 = "58C3C5D3410E80200C44D17FEC39BE8A21740A0577B231F26A436995EE85E6D5F6A0E2779716B07351700B783C32FED69094B276E4A7C5F3336520FA4B16909DF1E83ECA5BB88213CEE7092A27BB56AEEF1EF3F3BFA77EDBFDF54B63AA2B7F9EDA6B2D308FFD278CDB6A7ED8CF1FC57CF67ACC0FF37FFA7F0E7E01BD9E5CD0";
  private String drawEllipticalPie_65536 = "58C385D3510280200803D01D7BC7AF30109CA87FF9C030887C17A8CBF6808EC72E2CE0E444C316F07966D42743AC19AD8F5A507D26FC05E580EC331B9D63390016901DD4179474520ED8BB49753F1D631BE57B79BF1039E25896383D66DE569C51D6D639E5ECD26FA491898F96DAB9712CEED56D3D5A9B8F97F9892636F3976750DCEF53FC32FFB7FFE7E20F931E46C8";
  private String clearScreen_65536 = "58C37BF60C1F606078362A3F2A3F2A3F68E50136C328C6";
  private String drawPolygon_65536 = "58C3ABA80002860A1C80810122CD40893C036E1B08CB2301ECF250ADD8DD80A48D814C79049B2CF90ABC60547E549E327900436A9510";
  private String drawPolyline_65536 = "58C3ABA80002860A1C80012AC340A13C2E0584E591007679B801F8E52B182894AF204FBE022F18951F95A74C1E00B8909858";
  private String drawTextStr_65536 = "58C3ABA8C00718182A46E547E547E507AD3C00AF07A5F0";
  private String drawTextChars_65536 = "58C3ABA8C00718182A46E547E547E507AD3C00AF07A5F0";
  private String drawHatchedRect_65536 = "58C36360C00D2A2A20242EC05081579A907652E419F0CB336057009367C06102D1F2381CCA802C811270484288D0C26011235F315CE4E1C20CB8E21F2DF4A8997E06AD7C0581FC8592AEB0E44F00577F5988";
  private String fillHatchedRect_65536 = "58C3CDD3410A00200844D17F6C4F3CE7A84510181A5154B3F4890429A6306690706D00A539E6E44E30A8952168D8F70EF42CB9B6DCBDE563D7C8EEFF07D6B5FD7AE6B3FBB2C97D166B62A1B7";
  private String copyRect_65536 = "58C3D5D0210200211042518F4DF9775777824598B441EAA300ACC8658CC8E5CAAEECCAAE379C8A714E6E0EAEF039D8C276F085E5100ABDF3B3DF5E3DCDC7BDD9D7EC6FFEA9FF268DFDAB22";
  private String copyImageRect_65536 = "58C3EDC9210A00300C04C13CFBCCFEBD50A82B6B425CC64E7201F9A87A6B8F3FFEF8D37B863F6A7F7FF60F4B75A771";
  private String setClipR_65536 = "58C37BF60C0554A002068667A3F2A4C8A381819747B875547E38CA0300ED8DA7CF";
  private String drawImage_65536 = "58C3EDD2310EC0200C43518EEDE5DFBD034394343862E9544F88679088905CD6D2EF77CECEC189740EB9501C4A213B35D979E7C6F9CC63C3F8707EB8DFBE3F9607EFE7A7C9E5E7DF14DCFFD9FE002232AC3A";
  private String drawArrow_65536 = "58C3BDD14B0EC0200804508ECD89E71CFD50A38B61C6A469D9C90B0A18F144B238D38A2F4FC1B767CFE5D972F6955F78682773AC19B6872541F738CF41FFE1DFF947403BB4433BB403EFEAEDFBB6FF9A1F667F7589723887736CD41FD23EA168";

  private String setPixel_256 = "58C385D3C116C0100C44D1F96C9F5F5534613263E9BE8543A0A905341897C1EB2A182E82CFEB607A192CAF82ED45F03B0F82D3203A0B929320FB1D1C7E05B8368C1F01C8918CA700F4528C8700C5B318DF01CAC1303E0388D1343E02C8CF61BC07DA3BE3013A6997E0";
  private String drawLine_256 = "58C38D935B1280200C0373EC3DBE8EA3435F10FBBB29943448AED8D78375C437DD09C47BBC36F8E3A3402C3E0844E44D20322F0251791288CCCB9082DE1FBA9839ED2DD5FE6A565B4FD946F74F4C3C8C36F6FFBCDFCC6FDE6FFC3BF9EFF767F66FF263F267F26BF26FFE8FA90BB62F54D8";
  private String drawRect_256 = "58C3636020042A700302D25493C7E50EB83C14D34C1E8FFD70092C410666C3E511DAB0EBC72E8F293A5CE57184DF20487F03294F000000B5C45280";
  private String fillRect_256 = "58C37BF6AC020F6060784640BEA2820EF2CF406070CA23249E210015E511768D74F9014C7F83581E003FBCEA42";
  private String fillPie_256 = "58C3D5924B0EC0200844E7D87362CE518592CA27BA6AD2CE06E10D60A2E04E187A9BE33FBCD86301C8869003C5B0A6403068C85844DC334E95BB41A63A1E742F79B8B5D970EF67B7DFC22C92AC0BB4D76A6C0CD273169C384F5C25E1D2DDA3AE0379F8FF5FE7176DB35F82";
  private String fillEllipticalPie_256 = "58C3A593511280200805F7D89C9873540EA908659A1F0EB24F640045DE16C88433E3ACF0518E1790E83B078C02778620E88FC5D6246267ABEA2E37EC05F17AE5650FE9192F26AE5EB7575B20D73FE20ABCBE7FEED9FD153E8B1FE6CBA58F6C71EB5FCA5B052A96649AAE180F5C42D5C6E9B67EF0FDFFFCE5074E2F5668";
  private String fillPolygon_256 = "58C3ABA800826715380003430511F2CF9E3DA344FED9339C0A1818A072389480E52B70DB418C3C9449AE3C9CFD0C77F8551008DF51F9517932E5019FEDB86A";
  private String drawRoundRect_256 = "58C363A8A860C0012A4052405C81033080E5714A8314404CC1AD8058791CEA60F20C15D89D492D797CF633A070183084E0063320071C36FDD8E52B468C3C8EF0A346FA19387942F98360FE22903F01CACA5AF0";
  private String fillRoundRect_256 = "58C3E5D3C10A00200803D07DF6BED8EFC820100FCEA0A0433BF6265E12A4152109D43C0BEE26E26E32BB8EA2B89E81A270C9C5FE004492A3713D9F77FDEC27FFE79D77F7D1DE57739F03E2659811";
  private String draw3dRect_256 = "58C36360C0039E31E00537C0F21538C1C8927F06030C38E46F4000034E79083D64E5E1E902BB3C0301F9119A7E6EE00140F967780103030018FD9C98";
  private String drawCircle_256 = "58C38D530112802008DBB3F7FC3A01194A9AD59DB836262208C6808D8CDF95092750663E179686C1C7A69F347820AAF307C3B1261841ACCEA0F8B1CFF54B829487E42FFB0EF6F05F2C62B2E195F02D8A094D1F78C2099A9E640B175081A063916EF0E65D707B58777DE673CDDFE1E21F5AF4CD7DA92E518BD3D78FE7FAE7BC3FBFEBF9F3D23FBCF41F2FFD7BEE7FF0CFFDF9BE7F0FEEC92850";
  private String fillCircle_256 = "58C3AD93CB15C0200804297B2BDE3AF2310A8BA897703026333E0D82611776C77F9C2D1237A56238A746E66429744ED6C2C7C98590F8FB3A73C122083753A1F380FDD184B07C7C7721F3671CB392FB30380EEBB1DFDF93DBCF4F393F5498FE3F5E5E953F4C82E61FC0F6FEB01210F9BA7E500BC87C55BFB11BAAFAC7A1FF4EFC02D0D00E2D";
  private String drawDots_256 = "58C38D93611A80200843776C4EBC73649A25082EFDE357BC090341D4BB2DD2EA754718CADF9D6F51153D782B14D005382233DA269F29E049805F74A4173E2AE02D809EF0E7C9C32B60318009E5FD8BB7C219C8ADE6E8BFCF1BA101F492DCFBB77E2EF89FF78BFC45FDC2BF93FFBA7FA2FF627EC4FC89F915F37F7E3FE071E3029C0BA050";
  private String drawDottedRect_256 = "58C36378C6801B02C1B30A3C00A4029FFC88D2CF30C2FD3F30FAC1C99480FE6704ECA7443F38A3D0D0FF0CCFF042060025ABA370";
  private String setClip_256 = "58C37BF60C0554A002068667A3F2A4C8A381819747B875547E38CA0300ED8DA7CF";
  private String translate_256 = "58C3D5924B12C020084373EC1CBF53DB2A099F7D59E8C803F9924BF0084111E05C386FE3AF11B3C176DC5C23DC5C31C52064043D9C8335CF381A7C5C8173985EF80E71D4F97FD3563C665DE42F5DB1FAA15DD7FAE133B3FE71C032BF02CBFC0B2CFBC3767FC2FEB1E18DFC9C63E698FDD7E42F69217AD0";
  private String drawEllipse_256 = "58C3CD934912C0200804E7D9F3FC94904244961CC3456B1A954D721BD40EC5C36B47A791C9395B190D9EDFF8D594675855E139167DF10A2FF281E38C38A4B979E689F97CFD7A28F55F79EB21F9A3BD1ED66A94F545175EDD42EB6FCB09B4F35371ECFC87F91DE77FFC3FF5FF7B00F0F67BC0";
  private String fillEllipse_256 = "58C3DDD2410AC0200C44518F3D27FEE7705108C6D8A1E0A26276F22681102557ADE912C73BB0D51F53F29CEC300792430458380C81637C78557E1CB73F25A0EABCDF377C99FAD82FEFF2FEEBFFEE54DDE588";
  private String drawArc_256 = "58C38D93010EC0200803FBEC3E7FD998836A279A18B58744B182D9104D1422E1324B2EBB7239B8D254EE3838FC6AC11D8E80877B1C897F928F80035ED3AFE5110E4E77558E698C7C22477A94F34370E98E7FB2E728130CCE735EFBC2F5FC58FCA3F747D687A67EA0E359FFC29DB7B07D5FE8FBB3F10F1BFFB1F16FE7FFE6FF70FFFF2E51566108";
  private String drawPie_256 = "58C38D93010E80300803FBEC3EDF288E5299E21213C741C780815A88651642B0FD895B94B68B3B95E5F4C30EDFB6E03B1C0E17DFE3107E115F0E3F7895EFE531DE2EE31CE5939E0A9CA54556B99C170044A6D179E4BB6C8DE3B1BC9579044CFF913D5095E1B7A702DBFC64CB4B0D603C1D4AB8373FE7B3F0DEBF4CEAA5BFF0FE73981F0EF3C7617EA7F91FDE0FBFDFDF012A154FB0";
  private String drawEllipticalArc_256 = "58C3C5D3410E80200C44D17FEC39BE8A21740A0577B231F26A436995EE85E6D5F6A0E2779716B07351700B783C32FED69094B276E4A7C5F3336520FA4B16909DF1E83ECA5BB88213CEE7092A27BB56AEEF1EF3F3BFA77EDBFDF54B63AA2B7F9EDA6B2D308FFD278CDB6A7ED8CF1FC57CF67ACC0FF37FFA7F0E7E01BD9E5CD0";
  private String drawEllipticalPie_256 = "58C385D3510280200803D01D7BC7AF30109CA87FF9C030887C17A8CBF6808EC72E2CE0E444C316F07966D42743AC19AD8F5A507D26FC05E580EC331B9D63390016901DD4179474520ED8BB49753F1D631BE57B79BF1039E25896383D66DE569C51D6D639E5ECD26FA491898F96DAB9712CEED56D3D5A9B8F97F9892636F3976750DCEF53FC32FFB7FFE7E20F931E46C8";
  private String clearScreen_256 = "58C37BF60C1F606078362A3F2A3F2A3F68E50136C328C6";
  private String drawPolygon_256 = "58C3ABA80002860A1C80810122CD40893C036E1B08CB2301ECF250ADD8DD80A48D814C79049B2CF90ABC60547E549E327900436A9510";
  private String drawPolyline_256 = "58C3ABA80002860A1C80012AC340A13C2E0584E591007679B801F8E52B182894AF204FBE022F18951F95A74C1E00B8909858";
  private String drawTextStr_256 = "58C3ABA8C00718182A46E547E547E507AD3C00AF07A5F0";
  private String drawTextChars_256 = "58C3ABA8C00718182A46E547E547E507AD3C00AF07A5F0";
  private String drawHatchedRect_256 = "58C36360C00D2A2A20242EC05081579A907652E419F0CB336057009367C06102D1F2381CCA802C811270484288D0C26011235F315CE4E1C20CB8E21F2DF4A8997E06AD7C0581FC8592AEB0E44F00577F5988";
  private String fillHatchedRect_256 = "58C3CDD3410A00200844D17F6C4F3CE7A84510181A5154B3F4890429A6306690706D00A539E6E44E30A8952168D8F70EF42CB9B6DCBDE563D7C8EEFF07D6B5FD7AE6B3FBB2C97D166B62A1B7";
  private String copyRect_256 = "58C3D5D0210200211042518F4DF9775777824598B441EAA300ACC8658CC8E5CAAEECCAAE379C8A714E6E0EAEF039D8C276F085E5100ABDF3B3DF5E3DCDC7BDD9D7EC6FFEA9FF268DFDAB22";
  private String copyImageRect_256 = "58C3EDC9210A00300C04C13CFBCCFEBD50A82B6B425CC64E7201F9A87A6B8F3FFEF8D37B863F6A7F7FF60F4B75A771";
  private String setClipR_256 = "58C37BF60C0554A002068667A3F2A4C8A381819747B875547E38CA0300ED8DA7CF";
  private String drawImage_256 = "58C3EDD2310EC0200C43518EEDE5DFBD034394343862E9544F88679088905CD6D2EF77CECEC189740EB9501C4A213B35D979E7C6F9CC63C3F8707EB8DFBE3F9607EFE7A7C9E5E7DF14DCFFD9FE002232AC3A";
  private String drawArrow_256 = "58C3BDD14B0EC0200804508ECD89E71CFD50A38B61C6A469D9C90B0A18F144B238D38A2F4FC1B767CFE5D972F6955F78682773AC19B6872541F738CF41FFE1DFF947403BB4433BB403EFEAEDFBB6FF9A1F667F7589723887736CD41FD23EA168";

  // void drawEllipse(int xc, int yc, int rx, int ry);
  private void testDrawEllipse()
  {
    resetImage();
    g.drawEllipse(15,15,15,15);
    g.drawEllipse(10,10,1,1);
    g.drawEllipse(10,20,1,0);
    g.drawEllipse(20,10,0,1);
    g.drawEllipse(-5,22,10,5);
  }

  // void fillEllipse(int xc, int yc, int rx, int ry);
  private void testFillEllipse()
  {
    resetImage();
    g.fillEllipse(15,15,5,5);
    g.fillEllipse(8,8,1,1);
    g.fillEllipse(10,22,1,0);
    g.fillEllipse(22,12,0,1);
    g.fillEllipse(-5,22,10,5);
  }

  // void drawArc(int xc, int yc, int r, double startAngle, double endAngle);
  private void testDrawArc()
  {
    resetImage();
    g.drawArc(15,15, 15,0,360);
    g.drawArc(15,15, 12, -15,15);
    g.drawArc(15,15, 9, 300,360);
    g.drawArc(15,15, 6, -700,800);
    g.drawArc(15,15, 3, -180,140);
    g.drawArc(15,15, 1, 1,1);
    g.drawArc(18,18, 1, 10,10);
    g.drawArc(8,8, 0, 10,10);
    g.drawArc(-18,8, 20, 0,360);
  }

  // void drawPie(int xc, int yc, int r, double startAngle, double endAngle);
  private void testDrawPie()
  {
    resetImage();
    g.drawPie(15,15, 15,0,360);
    g.drawPie(15,15, 12, -15,15);
    g.drawPie(15,15, 9, 300,360);
    g.drawPie(15,15, 6, -700,800);
    g.drawPie(15,15, 3, -180,140);
    g.drawPie(15,15, 1, 1,1);
    g.drawPie(18,18, 1, 10,10);
    g.drawPie(-18,8, 20, 0,360);
  }

  // void fillPie(int xc, int yc, int r, double startAngle, double endAngle);
  private void testFillPie()
  {
    resetImage();
    g.fillPie(15,15, 12, -15,15);
    g.backColor = Color.BLACK;
    g.fillPie(15,15, 9, 300,360);
    g.backColor = Color.WHITE;
    g.fillPie(15,15, 6, -700,800);
    g.backColor = Color.BLACK;
    g.fillPie(15,15, 3, -180,140);
    g.backColor = Color.WHITE;
    g.fillPie(15,15, 1, 1,1);
    g.backColor = Color.BLACK;
    g.fillPie(18,18, 1, 10,10);
    g.backColor = Color.WHITE;
    g.fillPie(-18,8, 20, 0,360);
  }

  // void drawEllipticalArc(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
  private void testDrawEllipticalArc()
  {
    resetImage();
    g.drawEllipticalArc(15,15, 15,20,0,360);
    g.drawEllipticalArc(15,15, 12,17, -15,15);
    g.drawEllipticalArc(15,15, 9,14, 300,360);
    g.drawEllipticalArc(15,15, 6,11, -700,800);
    g.drawEllipticalArc(15,15, 3,8, -180,140);
    g.drawEllipticalArc(15,15, 1,1, 1,1);
    g.drawEllipticalArc(18,18, 1,1, 10,10);
    g.drawEllipticalArc(8,8, 0,0, 10,10);
    g.drawEllipticalArc(-18,8, 20,14, 0,360);
  }

  // void drawEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
  private void testDrawEllipticalPie()
  {
    resetImage();
    g.drawEllipticalPie(15,15, 15,20,0,360);
    g.drawEllipticalPie(15,15, 12,17, -15,15);
    g.drawEllipticalPie(15,15, 9,14, 300,360);
    g.drawEllipticalPie(15,15, 6,11, -700,800);
    g.drawEllipticalPie(15,15, 3,8, -180,140);
    g.drawEllipticalPie(15,15, 1,1, 1,1);
    g.drawEllipticalPie(18,18, 1,1, 10,10);
    g.drawEllipticalPie(8,8, 0,0, 10,10);
    g.drawEllipticalPie(-18,8, 20,14, 0,360);
  }

  // void fillEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
  private void testFillEllipticalPie()
  {
    resetImage();
    g.fillEllipticalPie(15,15, 12,17, -15,15);
    g.backColor = Color.BLACK;
    g.fillEllipticalPie(15,15, 9,14, 300,360);
    g.backColor = Color.WHITE;
    g.fillEllipticalPie(15,15, 6,11, -700,800);
    g.backColor = Color.BLACK;
    g.fillEllipticalPie(15,15, 3,8, -180,140);
    g.backColor = Color.WHITE;
    g.fillEllipticalPie(15,15, 1,1, 1,1);
    g.backColor = Color.BLACK;
    g.fillEllipticalPie(18,18, 1,1, 10,10);
    g.backColor = Color.WHITE;
    g.fillEllipticalPie(-18,8, 20,14, 0,360);
  }

  // void drawCircle(int xc, int yc, int r);
  private void testDrawCircle()
  {
    resetImage();
    g.drawCircle(15,15, 15);
    g.drawCircle(15,15, 20);
    g.drawCircle(15,15, 12);
    g.drawCircle(15,15, 9);
    g.drawCircle(15,15, 6);
    g.drawCircle(15,15, 3);
    g.drawCircle(15,15, 1);
    g.drawCircle(18,18, 1);
    g.drawCircle(8,8, 0);
    g.drawCircle(-18,8, 20);
  }

  // void fillCircle(int xc, int yc, int r);
  private void testFillCircle()
  {
    resetImage();
    g.fillCircle(15,15, 12);
    g.backColor = Color.BLACK;
    g.fillCircle(15,15, 9);
    g.backColor = Color.WHITE;
    g.fillCircle(15,15, 6);
    g.backColor = Color.BLACK;
    g.fillCircle(15,15, 3);
    g.backColor = Color.WHITE;
    g.fillCircle(15,15, 1);
    g.backColor = Color.BLACK;
    g.fillCircle(18,18, 0);
    g.backColor = Color.WHITE;
    g.fillCircle(-18,8, 20);
  }

  // int getPixel(int x, int y);
  private void testGetPixel()
  {
    assertEquals(-1, g.getPixel(-1,-1));
    assertEquals(-1, g.getPixel(40,40));
  }

  // void setPixel(int x, int y);
  private void testSetPixel()
  {
    resetImage();
    for (int i = -10; i < 40; i++) {
      g.setPixel(i,i);
    }
  }

  // void drawLine(int Ax, int Ay, int Bx, int By);
  private void testDrawLine()
  {
    resetImage();
    g.drawLine(0,0,30,30);
    g.drawLine(0,0,30,0);
    g.drawLine(0,0,0,30);
    g.drawLine(30-1,0,30-1,30);
    g.drawLine(0,30-1,30,30-1);
    g.drawLine(30,0,0,30);
    g.drawLine(5,10,5,10); // a single pixel
    g.drawLine(5,15,6,16); // two pixels
  }

  // void clearScreen();
  private void testClearScreen()
  {
    resetImage();
    g.fillRect(0,0,totalcross.sys.Settings.screenWidth,totalcross.sys.Settings.screenHeight);
  }

  // void drawDots(int Ax, int Ay, int Bx, int By);
  private void testDrawDots()
  {
    resetImage();
    g.drawDots(0,0,30,30);
    g.drawDots(0,0,30,0);
    g.drawDots(0,0,0,30);
    g.drawDots(30-1,0,30-1,30);
    g.drawDots(0,30-1,30,30-1);
    g.drawDots(30,0,0,30);
    g.drawDots(5,10,5,10);
    g.drawDots(5,15,6,16);
  }

  // void drawRect(int x, int y, int w, int h);
  private void testDrawRect()
  {
    resetImage();
    g.drawRect(0,0,30,30);
    g.drawRect(0,0,0,0);
    g.drawRect(5,5,5,5);
    g.drawRect(10,10,11,11);
    g.drawRect(40,40,10,10);
    g.drawRect(-1,10,4,3);
    g.drawRect(-8,-8,10,10);
  }

  // void fillRect(int x, int y, int w, int h);
  private void testFillRect()
  {
    resetImage();
    g.fillRect(0,0,0,0);
    g.fillRect(5,5,5,5);
    g.fillRect(10,10,11,11);
    g.fillRect(40,40,10,10);
    g.fillRect(-1,10,4,3);
    g.fillRect(-8,-8,10,10);
  }

  // void drawDottedRect(int x, int y, int w, int h);
  private void testDrawDottedRect()
  {
    resetImage();
    g.drawDottedRect(0,0,30,30);
    g.drawDottedRect(10,10,1,1);
    g.drawDottedRect(10,20,1,0);
    g.drawDottedRect(20,10,0,1);
    g.drawDottedRect(-5,22,10,5);
  }

  // void fillPolygon(int []xPoints, int []yPoints, int nPoints);
  private void testFillPolygon()
  {
    resetImage();

    int []x = {0,0,10,10,5};
    int []y = {10,5,5,10,0};
    g.fillPolygon(x, y, 5);
  }

  // void drawPolygon(int []xPoints, int []yPoints, int nPoints);
  private void testDrawPolygon()
  {
    resetImage();
    int []x = {0,0,10,10,5};
    int []y = {10,5,5,10,0};
    g.drawPolygon(x, y, 5);
  }

  // void drawPolyline(int []xPoints, int []yPoints, int nPoints);
  private void testDrawPolyline()
  {
    resetImage();
    int []x = {0,0,10,10,5};
    int []y = {10,5,5,10,0};
    g.drawPolyline(x, y, 5);
  }

  // void drawText(String text, int x, int y);
  private void testDrawTextStr()
  {
    resetImage();
    // i hope that nothing will be drawn by these tests
    g.drawText("Michelle", -100,-100);
    g.drawText("",10,10);
  }

  // void drawText(char chars[], int start, int count, int x, int y);
  private void testDrawTextChars()
  {
    resetImage();
    g.drawText("Michelle".toCharArray(),2,6, -100,-100);
    g.drawText(new char[]{},0,0,10,10);
  }

  // void drawHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom);
  private void testDrawHatchedRect()
  {
    resetImage();
    g.drawHatchedRect(0,0,30,30,true,true);
    g.drawHatchedRect(0,0,0,0,true,true);
    g.drawHatchedRect(5,5,5,5,true,false);
    g.drawHatchedRect(10,10,11,11,false,true);
    g.drawHatchedRect(40,40,10,10,true,false);
    g.drawHatchedRect(-1,10,4,3,false,true);
    g.drawHatchedRect(-8,-8,10,10,true,true);
  }

  // void fillHatchedRect(int x, int y, int width, int height, boolean top, boolean bottom);
  private void testFillHatchedRect()
  {
    resetImage();
    g.fillHatchedRect(0,0,30,30,true,true);
    g.backColor = Color.WHITE;
    g.fillHatchedRect(5,5,5,5,true,false);
    g.fillHatchedRect(10,10,11,11,false,true);
    g.fillHatchedRect(40,40,10,10,true,false);
    g.fillHatchedRect(-1,10,4,4,false,true);
    g.fillHatchedRect(-8,-8,10,10,true,true);
  }

  // void drawRoundRect(int x, int y, int width, int height, int r);
  private void testDrawRoundRect()
  {
    resetImage();
    g.drawRoundRect(0,0,30,30,5);
    g.drawRoundRect(0,0,0,0,2);
    g.drawRoundRect(5,5,5,5,1);
    g.drawRoundRect(10,10,11,11,0);
    g.drawRoundRect(40,40,10,10,5);
    g.drawRoundRect(-1,10,4,3,10);
    g.drawRoundRect(-8,-8,10,10,10);
  }

  // void fillRoundRect(int x, int y, int width, int height, int r);
  private void testFillRoundRect()
  {
    resetImage();
    g.fillRoundRect(0,0,30,30,5);
    g.backColor = Color.WHITE;
    g.fillRoundRect(0,0,0,0,2);
    g.fillRoundRect(5,5,5,5,1);
    g.fillRoundRect(10,10,11,11,0);
    g.fillRoundRect(40,40,10,10,5);
    g.fillRoundRect(-1,10,4,3,10);
    g.fillRoundRect(-8,-8,10,10,10);
  }

  // void setClip(int x, int y, int w, int h);
  private void testSetClip() // also tests clearClip
  {
    resetImage();
    g.setClip(0,0,15,15);
    g.fillRect(0,0,30,30);
    g.clearClip();
    g.fillRect(10,10,20,20);
  }

  // boolean clip(Rect r);
  private void testClip() // also tests getClip
  {
    assertTrue(g.clip(new Rect(10,10,10,10)));
    assertFalse(g.clip(new Rect(-10,-10,10,10)));
  }

  // void translate(int dx, int dy);
  private void testTranslate() // also tests getTranslation
  {
    resetImage();         assertEquals(new Coord(0,0), g.getTranslation());
    g.translate(10,10);   assertEquals(new Coord(10,10), g.getTranslation());
    g.drawCircle(0,0,10);
    g.translate(-10,-10);
    g.drawCircle(0,0,10);
    g.translate(35,35);
    g.drawCircle(0,0,10);
  }

  // void copyRect(ISurface surface, int x, int y, int width, int height, int dstX, int dstY);
  private void testCopyRect()
  {
    resetImage();
    // prepare image to be copied
    Image img = createBall();

    g.copyRect(img, 0, -10, 20, 20, 0,0);
    g.copyRect(img, 10,10, 10,10, 20,20);
    g.copyRect(img, 10,10, 10,10, -5,-5);
    // TODO TAKE THE PROBLEMS OUT OF GFORGE AND TEST HERE
  }

  // void copyImageRect(waba.fx.Image image, int x, int y, int width, int height, int drawOp, waba.fx.Color backColor, boolean doClip);
  private void testCopyImageRect()
  {
    resetImage();
    ImageTester ball = createBall();
    g.copyImageRect(ball, 0,0,10,10, false);
  }

  // void setClip(Rect r)
  private void testSetClipR()
  {
    resetImage();
    g.setClip(new Rect(0,0,15,15));
    g.fillRect(0,0,30,30);
    g.clearClip();
    g.fillRect(10,10,20,20);
  }

  // void drawImage(waba.fx.Image image, int x, int y)
  private void testDrawImage()
  {
    resetImage();
    Image ball = createBall();
    g.drawImage(ball, 10,10);
    g.drawImage(ball, 20,20);
  }

  // void draw3dRect(int x, int y, int width, int height, byte type, boolean yMirror, boolean simple, Color []fourColors)
  private void testDraw3dRect() // also tests compute3dColors
  {
    resetImage();
    int []fourColors = new int[4];
    Graphics.compute3dColors(true,Color.BLACK,Color.WHITE,fourColors);
    g.draw3dRect(0,0,30,30,Graphics.R3D_LOWERED,false,false,fourColors);
    g.draw3dRect(10,10,10,10,Graphics.R3D_RAISED,false,false,fourColors);
  }

  // void drawArrow(int x, int y, int h, byte type, boolean pressed, boolean enabled, Color fore)
  private void testDrawArrow()
  {
    resetImage();
    int w = 5;
    g.drawArrow(0,0,w,Graphics.ARROW_DOWN,false,Color.WHITE); // here is h regardless the case
    g.drawArrow(8,8,w,Graphics.ARROW_UP,false,Color.WHITE); // here is h regardless the case
    g.drawArrow(16,16,w,Graphics.ARROW_LEFT,false,Color.BLACK); // here is h regardless the case
    g.drawArrow(24,24,w,Graphics.ARROW_RIGHT,false,Color.BLACK); // here is h regardless the case
  }

  private ImageTester createBall()
  {
    ImageTester img=null;
    try
    {
      img = new ImageTester(20,20);
    }
    catch (ImageException e)
    {
      fail(e.getMessage());
    }
    Graphics gg = img.getGraphics();
    gg.backColor = back;
    gg.fillRect(0,0,20,20);
    gg.backColor = Color.RED;
    gg.fillCircle(10,10,10);
    return img;
  }

  private void resetImage()
  {
    if (debuggingOnScreen)
    {
      g = MainWindow.getMainWindow().getGraphics();
      g.setClip(0,0,30,30);
    }
    g.backColor = back;
    g.fillRect(0,0,30,30);
    g.backColor = Color.BLACK;
    g.foreColor = Color.WHITE;
  }

  @Override
  public void testRun()
  {
    //recording = true;
    super.testRun(); // important!

    try
    {
      it = new ImageTester(30,30);
    }
    catch (ImageException e)
    {
      fail(e.getMessage());
    }
    g = it.g;

    // trouble
    if (Settings.onJavaSE){
      testSetPixel();
    }           assertOK(setPixel_256,setPixel_65536,                      "setPixel");
    testDrawLine();           assertOK(drawLine_256,drawLine_65536,                      "drawLine");
    testDrawRect();           assertOK(drawRect_256,drawRect_65536,                      "drawRect");
    testFillRect();           assertOK(fillRect_256,fillRect_65536,                      "fillRect");
    testFillPie();            assertOK(fillPie_256,fillPie_65536,                        "fillPie");
    testFillEllipticalPie();  assertOK(fillEllipticalPie_256,fillEllipticalPie_65536,    "fillEllipticalPie");
    testFillPolygon();        assertOK(fillPolygon_256,fillPolygon_65536,                "fillPolygon");
    testDrawRoundRect();      assertOK(drawRoundRect_256,drawRoundRect_65536,            "drawRoundRect");
    testFillRoundRect();      assertOK(fillRoundRect_256,fillRoundRect_65536,            "fillRoundRect");
    testDraw3dRect();         assertOK(draw3dRect_256,draw3dRect_65536,                  "draw3dRect");
    testDrawCircle();         assertOK(drawCircle_256,drawCircle_65536,                  "drawCircle");
    testFillCircle();         assertOK(fillCircle_256,fillCircle_65536,                  "fillCircle");
    testDrawDots();           assertOK(drawDots_256,drawDots_65536,                      "drawDots");
    testDrawDottedRect();     assertOK(drawDottedRect_256,drawDottedRect_65536,          "drawDottedRect");
    testClip();
    testSetClip();            assertOK(setClip_256,setClip_65536,                        "setClip");
    testTranslate();          assertOK(translate_256,translate_65536,                    "translate");
    testDrawEllipse();        assertOK(drawEllipse_256,drawEllipse_65536,                "drawEllipse");
    testFillEllipse();        assertOK(fillEllipse_256,fillEllipse_65536,                "fillEllipse");
    testDrawArc();            assertOK(drawArc_256,drawArc_65536,                        "drawArc");
    testDrawPie();            assertOK(drawPie_256,drawPie_65536,                        "drawPie");
    testDrawEllipticalArc();  assertOK(drawEllipticalArc_256,drawEllipticalArc_65536,    "drawEllipticalArc");
    testDrawEllipticalPie();  assertOK(drawEllipticalPie_256,drawEllipticalPie_65536,    "drawEllipticalPie");
    testGetPixel();
    testClearScreen();        assertOK(clearScreen_256,clearScreen_65536,                "clearScreen");
    testDrawPolygon();        assertOK(drawPolygon_256,drawPolygon_65536,                "drawPolygon");
    testDrawPolyline();       assertOK(drawPolyline_256,drawPolyline_65536,              "drawPolyline");
    testDrawTextStr();        assertOK(drawTextStr_256,drawTextStr_65536,                "drawTextStr");
    testDrawTextChars();      assertOK(drawTextChars_256,drawTextChars_65536,            "drawTextChars");
    testDrawHatchedRect();    assertOK(drawHatchedRect_256,drawHatchedRect_65536,        "drawHatchedRect");
    testFillHatchedRect();    assertOK(fillHatchedRect_256,fillHatchedRect_65536,        "fillHatchedRect");
    testCopyRect();           assertOK(copyRect_256,copyRect_65536,                      "copyRect");
    testCopyImageRect();      assertOK(copyImageRect_256,copyImageRect_65536,            "copyImageRect");
    testSetClipR();           assertOK(setClipR_256,setClipR_65536,                      "setClipR");
    testDrawImage();          assertOK(drawImage_256,drawImage_65536,                    "drawImage");
    testDrawArrow();          assertOK(drawArrow_256,drawArrow_65536,                    "drawArrow");
  }
}
