/*++
THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY OF
ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
PARTICULAR PURPOSE.
Copyright (c) Microsoft Corporation. All rights reserved.

-----------------------------------------------------------------------------

@doc EXTERNAL

@module RIL.H - Radio Interface Layer


-----------------------------------------------------------------------------
--*/

#ifndef _RIL_H_
#define _RIL_H_

#include <windows.h>
#ifdef __cplusplus
extern "C" {
#endif

#define RIL_DRIVER_VERSION                    0x00020000

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Error Class | Each RIL error falls into a general error class bucket
//
// @comm In RIL, the low order 16 bits are divided into an 8-bit error class and
//       an 8-bit error value.  Use the RILERRORCLASS macro to obtain the error
//       class from a RIL HRESULT.
//
// -----------------------------------------------------------------------------
#define RIL_ERRORCLASS_NONE                         0x00  // @constdefine Misc error
#define RIL_ERRORCLASS_PASSWORD                     0x01  // @constdefine Unspecified phone failure
#define RIL_ERRORCLASS_SIM                          0x02  // @constdefine Problem with the SIM
#define RIL_ERRORCLASS_NETWORKACCESS                0x03  // @constdefine Can't access the network
#define RIL_ERRORCLASS_NETWORK                      0x04  // @constdefine Error in the network
#define RIL_ERRORCLASS_MOBILE                       0x05  // @constdefine Error in the mobile
#define RIL_ERRORCLASS_NETWORKUNSUPPORTED           0x06  // @constdefine Unsupported by the network
#define RIL_ERRORCLASS_MOBILEUNSUPPORTED            0x07  // @constdefine Unsupported by the mobile
#define RIL_ERRORCLASS_BADPARAM                     0x08  // @constdefine An invalid parameter was supplied
#define RIL_ERRORCLASS_STORAGE                      0x09  // @constdefine Error relating to storage
#define RIL_ERRORCLASS_SMSC                         0x0A  // @constdefine Error relates to the SMSC
#define RIL_ERRORCLASS_DESTINATION                  0x0B  // @constdefine Error in the destination mobile
#define RIL_ERRORCLASS_DESTINATIONUNSUPPORTED       0x0C  // @constdefine Unsupported by destination mobile
#define RIL_ERRORCLASS_RADIOUNAVAILABLE             0x0D  // @constdefine The Radio Module is Off or a radio module may not be present
#define RIL_ERRORCLASS_GPRS                         0x0E  // @constdefine GPRS related failures

#define MAKE_RILERROR(errclass,code) \
    ((unsigned long) (errclass) << 8) | ((unsigned long)(code))

#define RILERRORCLASS(rilerror) \
    ((unsigned long) (((rilerror) >> 8) & 0xff))

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Error | Error codes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define FACILITY_RIL                    0x100

#define RIL_E_PHONEFAILURE              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_MOBILE,0x01)))  // @constdefine Unspecified phone failure
#define RIL_E_NOCONNECTION              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_MOBILE,0x02)))  // @constdefine RIL has no connection to the phone
#define RIL_E_LINKRESERVED              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_MOBILE,0x03)))  // @constdefine RIL's link to the phone is reserved
#define RIL_E_OPNOTALLOWED              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_MOBILEUNSUPPORTED,0x04)))  // @constdefine Attempted operation isn't allowed
#define RIL_E_OPNOTSUPPORTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_MOBILEUNSUPPORTED,0x05)))  // @constdefine Attempted operation isn't supported
#define RIL_E_PHSIMPINREQUIRED          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x06)))  // @constdefine PH-SIM PIN is required to perform this operation
#define RIL_E_PHFSIMPINREQUIRED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x07)))  // @constdefine PH-FSIM PIN is required to perform this operation
#define RIL_E_PHFSIMPUKREQUIRED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x08)))  // @constdefine PH-FSIM PUK is required to perform this operation
#define RIL_E_SIMNOTINSERTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x09)))  // @constdefine SIM isn't inserted into the phone
#define RIL_E_SIMPINREQUIRED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x0a)))  // @constdefine SIM PIN is required to perform this operation
#define RIL_E_SIMPUKREQUIRED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x0b)))  // @constdefine SIM PUK is required to perform this operation
#define RIL_E_SIMFAILURE                (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x0c)))  // @constdefine SIM failure was detected
#define RIL_E_SIMBUSY                   (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x0d)))  // @constdefine SIM is busy
#define RIL_E_SIMWRONG                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x0e)))  // @constdefine Inorrect SIM was inserted
#define RIL_E_INCORRECTPASSWORD         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x0f)))  // @constdefine Incorrect password was supplied
#define RIL_E_SIMPIN2REQUIRED           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x10)))  // @constdefine SIM PIN2 is required to perform this operation
#define RIL_E_SIMPUK2REQUIRED           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x11)))  // @constdefine SIM PUK2 is required to perform this operation
#define RIL_E_MEMORYFULL                (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_STORAGE,0x12)))  // @constdefine Storage memory is full
#define RIL_E_INVALIDINDEX              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_STORAGE,0x13)))  // @constdefine Invalid storage index was supplied
#define RIL_E_NOTFOUND                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_STORAGE,0x14)))  // @constdefine A requested storage entry was not found
#define RIL_E_MEMORYFAILURE             (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_STORAGE,0x15)))  // @constdefine Storage memory failure
#define RIL_E_TEXTSTRINGTOOLONG         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x16)))  // @constdefine Supplied text string is too long
#define RIL_E_INVALIDTEXTSTRING         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x17)))  // @constdefine Supplied text string contains invalid characters
#define RIL_E_DIALSTRINGTOOLONG         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x18)))  // @constdefine Supplied dial string is too long
#define RIL_E_INVALIDDIALSTRING         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x19)))  // @constdefine Supplied dial string contains invalid characters
#define RIL_E_NONETWORKSVC              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x1a)))  // @constdefine Network service isn't available
#define RIL_E_NETWORKTIMEOUT            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x1b)))  // @constdefine Network operation timed out
#define RIL_E_EMERGENCYONLY             (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x1c)))  // @constdefine Network can only be used for emergency calls
#define RIL_E_NETWKPINREQUIRED          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x1d)))  // @constdefine Network Personalization PIN is required to perform this operation
#define RIL_E_NETWKPUKREQUIRED          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x1e)))  // @constdefine Network Personalization PUK is required to perform this operation
#define RIL_E_SUBSETPINREQUIRED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x1f)))  // @constdefine Network Subset Personalization PIN is required to perform this operation
#define RIL_E_SUBSETPUKREQUIRED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x20)))  // @constdefine Network Subset Personalization PUK is required to perform this operation
#define RIL_E_SVCPINREQUIRED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x21)))  // @constdefine Service Provider Personalization PIN is required to perform this operation
#define RIL_E_SVCPUKREQUIRED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x22)))  // @constdefine Service Provider Personalization PUK is required to perform this operation
#define RIL_E_CORPPINREQUIRED           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x23)))  // @constdefine Corporate Personalization PIN is required to perform this operation
#define RIL_E_CORPPUKREQUIRED           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_PASSWORD,0x24)))  // @constdefine Corporate Personalization PUK is required to perform this operation
#define RIL_E_TELEMATICIWUNSUPPORTED    (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x25)))  // @constdefine Telematic interworking isn't supported
#define RIL_E_SMTYPE0UNSUPPORTED        (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x26)))  // @constdefine Type 0 messages aren't supported
#define RIL_E_CANTREPLACEMSG            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x27)))  // @constdefine Existing message cannot be replaced
#define RIL_E_PROTOCOLIDERROR           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x28)))  // @constdefine Uspecified error related to the message Protocol ID
#define RIL_E_DCSUNSUPPORTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x29)))  // @constdefine Specified message Data Coding Scheme isn't supported
#define RIL_E_MSGCLASSUNSUPPORTED       (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x2a)))  // @constdefine Specified message class isn't supported
#define RIL_E_DCSERROR                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x2b)))  // @constdefine Unspecified error related to the message Data Coding Scheme
#define RIL_E_CMDCANTBEACTIONED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x2c)))  // @constdefine Specified message Command cannot be executed
#define RIL_E_CMDUNSUPPORTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x2d)))  // @constdefine Specified message Command isn't supported
#define RIL_E_CMDERROR                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x2e)))  // @constdefine Unspecified error related to the message Command
#define RIL_E_MSGBODYHEADERERROR        (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x2f)))  // @constdefine Unspecified error related to the message Body or Header
#define RIL_E_SCBUSY                    (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x30)))  // @constdefine Message Service Center is busy
#define RIL_E_NOSCSUBSCRIPTION          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x31)))  // @constdefine No message Service Center subscription
#define RIL_E_SCSYSTEMFAILURE           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x32)))  // @constdefine Message service Center system failure occurred
#define RIL_E_INVALIDADDRESS            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x33)))  // @constdefine Specified address is invalid
#define RIL_E_DESTINATIONBARRED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x34)))  // @constdefine Message destination is barred
#define RIL_E_REJECTEDDUPLICATE         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x35)))  // @constdefine Duplicate message was rejected
#define RIL_E_VPFUNSUPPORTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x36)))  // @constdefine Specified message Validity Period Format isn't supported
#define RIL_E_VPUNSUPPORTED             (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x37)))  // @constdefine Specified message Validity Period isn't supported
#define RIL_E_SIMMSGSTORAGEFULL         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_STORAGE,0x38)))  // @constdefine Message storage on the SIM is full
#define RIL_E_NOSIMMSGSTORAGE           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x39)))  // @constdefine SIM isn't capable of storing messages
#define RIL_E_SIMTOOLKITBUSY            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x3a)))  // @constdefine SIM Application Toolkit is busy
#define RIL_E_SIMDOWNLOADERROR          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SIM,0x3b)))  // @constdefine SIM data download error
#define RIL_E_MSGSVCRESERVED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x3c)))  // @constdefine Messaging service is reserved
#define RIL_E_INVALIDMSGPARAM           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x3d)))  // @constdefine One of the message parameters is invalid
#define RIL_E_UNKNOWNSCADDRESS          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_SMSC,0x3e)))  // @constdefine Unknown message Service Center address was specified
#define RIL_E_UNASSIGNEDNUMBER          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_DESTINATION,0x3f)))  // @constdefine Specified message destination address is a currently unassigned phone number
#define RIL_E_MSGBARREDBYOPERATOR       (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x40)))  // @constdefine Message sending was barred by an operator
#define RIL_E_MSGCALLBARRED             (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x41)))  // @constdefine Message sending was prevented by outgoing calls barring
#define RIL_E_MSGXFERREJECTED           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_DESTINATION,0x42)))  // @constdefine Sent message has been rejected by the receiving equipment
#define RIL_E_DESTINATIONOUTOFSVC       (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_DESTINATION,0x43)))  // @constdefine Message could not be delivered because destination equipment is out of service
#define RIL_E_UNIDENTIFIEDSUBCRIBER     (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x44)))  // @constdefine Sender's mobile ID isn't registered
#define RIL_E_SVCUNSUPPORTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x45)))  // @constdefine Requested messaging service isn't supported
#define RIL_E_UNKNOWNSUBSCRIBER         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x46)))  // @constdefine Sender isn't recognized by the network
#define RIL_E_NETWKOUTOFORDER           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x47)))  // @constdefine Long-term network failure
#define RIL_E_NETWKTEMPFAILURE          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x48)))  // @constdefine Short-term network failure
#define RIL_E_CONGESTION                (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x49)))  // @constdefine Operation failed because of the high network traffic
#define RIL_E_RESOURCESUNAVAILABLE      (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x4a)))  // @constdefine Unspecified resources weren't available
#define RIL_E_SVCNOTSUBSCRIBED          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x4b)))  // @constdefine Sender isn't subscribed for the requested messaging service
#define RIL_E_SVCNOTIMPLEMENTED         (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x4c)))  // @constdefine Requested messaging service isn't implemented on the network
#define RIL_E_INVALIDMSGREFERENCE       (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x4d)))  // @constdefine Imvalid message reference value was used
#define RIL_E_INVALIDMSG                (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x4e)))  // @constdefine Message was determined to be invalid for unspecified reasons
#define RIL_E_INVALIDMANDATORYINFO      (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_BADPARAM,0x4f)))  // @constdefine Mandatory message information is invalid or missing
#define RIL_E_MSGTYPEUNSUPPORTED        (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x50)))  // @constdefine The message type is unsupported
#define RIL_E_ICOMPATIBLEMSG            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x51)))  // @constdefine Sent message isn't compatible with the network
#define RIL_E_INFOELEMENTUNSUPPORTED    (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x52)))  // @constdefine An information element specified in the message isn't supported
#define RIL_E_PROTOCOLERROR             (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x53)))  // @constdefine Unspefied protocol error
#define RIL_E_NETWORKERROR              (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x54)))  // @constdefine Unspecified network error
#define RIL_E_MESSAGINGERROR            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORK,0x55)))  // @constdefine Unspecified messaging error
#define RIL_E_NOTREADY                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x56)))  // @constdefine RIL isn't yet ready to perform the requested operation
#define RIL_E_TIMEDOUT                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x57)))  // @constdefine Operation timed out
#define RIL_E_CANCELLED                 (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x58)))  // @constdefine Operation was cancelled
#define RIL_E_NONOTIFYCALLBACK          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x59)))  // @constdefine Requested operation requires an RIL notification callback, which wasn't provided
#define RIL_E_OPFMTUNAVAILABLE          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKUNSUPPORTED,0x5a)))  // @constdefine Operator format isn't available
#define RIL_E_NORESPONSETODIAL          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NETWORKACCESS,0x5b)))  // @constdefine Dial operation hasn't received a response for a long time
#define RIL_E_SECURITYFAILURE           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x5c)))  // @constdefine Security failure
#define RIL_E_RADIOFAILEDINIT           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE,0x5d)))  // @constdefine Radio failed to initialize correctly
#define RIL_E_DRIVERINITFAILED          (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_RADIOUNAVAILABLE, 0x5e)))  // @constdefine There was a problem initializing the radio driver
#define RIL_E_RADIONOTPRESENT           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_RADIOUNAVAILABLE, 0x5f)))  // @constdefine The Radio is not present
#define RIL_E_RADIOOFF                  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_RADIOUNAVAILABLE, 0x60)))  // @constdefine The Radio is in Off mode
#define  RIL_E_ILLEGALMS                (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x61)))  // @constdefine Illegal MS
#define  RIL_E_ILLEGALME                (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x62)))  // @constdefine Illegal ME
#define  RIL_E_GPRSSERVICENOTALLOWED    (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x63)))  // @constdefine GPRS Service not allowed
#define  RIL_E_PLMNNOTALLOWED           (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x64)))  // @constdefine PLMN not allowed
#define  RIL_E_LOCATIONAREANOTALLOWED   (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x65)))  // @constdefine Location area not allowed
#define  RIL_E_ROAMINGNOTALLOWEDINTHISLOCATIONAREA  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x66)))  // @constdefine Roaming not allowed in this location area
#define  RIL_E_SERVICEOPTIONNOTSUPPORTED            (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x67)))  // @constdefine Service option not supported
#define  RIL_E_REQUESTEDSERVICEOPTIONNOTSUBSCRIBED  (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x68)))  // @constdefine Requested service option not subscribed
#define  RIL_E_SERVICEOPTIONTEMPORARILYOUTOFORDER   (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x69)))  // @constdefine Service option temporarily out of order
#define  RIL_E_PDPAUTHENTICATIONFAILURE             (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x6a)))  // @constdefine PDP authentication failure
#define  RIL_E_INVALIDMOBILECLASS                   (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x6b)))  // @constdefine invalid mobile class
#define  RIL_E_UNSPECIFIEDGPRSERROR                 (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_GPRS, 0x6c)))  // @constdefine unspecific GPRS error
#define  RIL_E_RADIOREBOOTED                 (MAKE_HRESULT(SEVERITY_ERROR, FACILITY_RIL, MAKE_RILERROR(RIL_ERRORCLASS_NONE, 0x6d)))  // @constdefine the command failed because the radio reset itself unexpectedly

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Class | Notification classes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NCLASS_FUNCRESULT                       (0x00000000)      // @constdefine API call results
#define RIL_NCLASS_CALLCTRL                         (0x00010000)      // @constdefine Call control notifications
#define RIL_NCLASS_MESSAGE                          (0x00020000)      // @constdefine Messaging notifications
#define RIL_NCLASS_NETWORK                          (0x00040000)      // @constdefine Network-related notifications
#define RIL_NCLASS_SUPSERVICE                       (0x00080000)      // @constdefine Supplementary service notifications
#define RIL_NCLASS_PHONEBOOK                        (0x00100000)      // @constdefine Phonebook notifications
#define RIL_NCLASS_SIMTOOLKIT                       (0x00200000)      // @constdefine SIM Toolkit notifications
#define RIL_NCLASS_MISC                             (0x00400000)      // @constdefine Miscellaneous notifications
#define RIL_NCLASS_RADIOSTATE                       (0x00800000)      // @constdefine Notifications Pertaining to changes in Radio State
#define RIL_NCLASS_NDIS                             (0x40000000) // @constdefine Nofitifcations that won't be picked up by all.
#define RIL_NCLASS_DEVSPECIFIC                      (0x80000000)      // @constdefine Reserved for device specific notifications
#define RIL_NCLASS_ALL                              (0x00ff0000)      // @constdefine All notification classes (except DevSpecifc)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants API Result | API call results (RIL_NCLASS_FUNCRESULT)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_RESULT_OK                               (0x00000001 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API call succeded; lpData is NULL
#define RIL_RESULT_NOCARRIER                        (0x00000002 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed because no carrier was detected; lpData is NULL
#define RIL_RESULT_ERROR                            (0x00000003 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed; lpData points to RIL_E_* constant
#define RIL_RESULT_NODIALTONE                       (0x00000004 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed because no dialtone was detected; lpData is NULL
#define RIL_RESULT_BUSY                             (0x00000005 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed because the line was busy; lpData is NULL
#define RIL_RESULT_NOANSWER                         (0x00000006 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed because of the lack of answer; lpData is NULL
#define RIL_RESULT_CALLABORTED                      (0x00000007 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed because it was cancelled prior to completion; lpData is NULL
#define RIL_RESULT_CALLDROPPED                      (0x00000008 | RIL_NCLASS_FUNCRESULT)  // @constdefine RIL API failed because the network dropped the call; lpData is NULL

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Call Control | Call control notifications (RIL_NCLASS_CALLCTRL)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_RING                             (0x00000001 | RIL_NCLASS_CALLCTRL)  // @constdefine Incoming call; lpData points to RILRINGINFO
#define RIL_NOTIFY_CONNECT                          (0x00000002 | RIL_NCLASS_CALLCTRL)  // @constdefine Data/voice connection has been established; lpData points to RILCONNECTINFO
#define RIL_NOTIFY_DISCONNECT                       (0x00000003 | RIL_NCLASS_CALLCTRL)  // @constdefine Data/voice connection has been terminated; lpData points to RIL_DISCINIT_* constant
#define RIL_NOTIFY_DATASVCNEGOTIATED                (0x00000004 | RIL_NCLASS_CALLCTRL)  // @constdefine Data connection service has been negotiated; lpData points to RILSERVICEINFO
#define RIL_NOTIFY_CALLSTATECHANGED                 (0x00000005 | RIL_NCLASS_CALLCTRL)  // @constdefine RIL has performed an operation that may have changed state of existing calls; lpData is NULL
#define RIL_NOTIFY_EMERGENCYMODEENTERED             (0x00000006 | RIL_NCLASS_CALLCTRL)  // @constdefine RIL has enetered emergency mode; lpData is NULL
#define RIL_NOTIFY_EMERGENCYMODEEXITED              (0x00000007 | RIL_NCLASS_CALLCTRL)  // @constdefine RIL has exited emergency mode; lpData is NULL
#define RIL_NOTIFY_EMERGENCYHANGUP                  (0x00000008 | RIL_NCLASS_CALLCTRL)  // @constdefine Existsing calls (if any) were hung up in RIL emergency mode; lpData is NULL
#define RIL_NOTIFY_HSCSDPARAMSNEGOTIATED            (0x00000009 | RIL_NCLASS_CALLCTRL)  // @constdefine HSCSD parameters for a call has been negotiated; lpData points to RILCALLHSCSDINFO
#define RIL_NOTIFY_DIAL                             (0x0000000A | RIL_NCLASS_CALLCTRL)  // @constdefine Outgoing call; lpData points to RILDIALINFO
#define RIL_NOTIFY_CALLPROGRESSINFO                 (0x0000000B | RIL_NCLASS_CALLCTRL)  // @constdefine CPI notification; lpData points to RILCALLINFO
#define RIL_NOTIFY_CURRENTLINECHANGED               (0x0000000C | RIL_NCLASS_CALLCTRL)  // @constdefine Current line has changed notification; lpData points to DWORD with new current address id
#define RIL_NOTIFY_GPRS_DISCONNECT                       (0x0000000D | RIL_NCLASS_CALLCTRL)  // @constdefine GPRS connection has been terminated; lpData points to RILGPRSCONTEXTACTIVATED sturct

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Messaging | Messaging notifications (RIL_MCLASS_MESSAGE)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_MESSAGE                          (0x00000001 | RIL_NCLASS_MESSAGE)  // @constdefine Incoming message; lpData points to RILMESSAGE
#define RIL_NOTIFY_BCMESSAGE                        (0x00000002 | RIL_NCLASS_MESSAGE)  // @constdefine Incoming broadcast message; lpData points to RILMESSAGE
#define RIL_NOTIFY_STATUSMESSAGE                    (0x00000003 | RIL_NCLASS_MESSAGE)  // @constdefine Incoming status-report message; lpData points to RILMESSAGE
#define RIL_NOTIFY_MSGSTORED                        (0x00000004 | RIL_NCLASS_MESSAGE)  // @constdefine A message has been added to storage; lpData points to the storage index assigned to the new message
#define RIL_NOTIFY_MSGDELETED                       (0x00000005 | RIL_NCLASS_MESSAGE)  // @constdefine A message has been deleted from storage; lpData points to the storage index occupied by the deleted message
#define RIL_NOTIFY_MSGSTORAGECHANGED                (0x00000006 | RIL_NCLASS_MESSAGE)  // @constdefine One of the message storage locations has been changed; lpData points to RILMSGSTORAGEINFO
#define RIL_NOTIFY_MESSAGE_IN_SIM                   (0x00000007 | RIL_NCLASS_MESSAGE)  // @constdefine Incoming message stored to SIM; lpData points to the storage RILMESSAGE_IN_SIM
#define RIL_NOTIFY_BCMESSAGE_IN_SIM                 (0x00000008 | RIL_NCLASS_MESSAGE)  // @constdefine Incoming broadcast message stored to SIM; lpData points to RILMESSAGE_IN_SIM
#define RIL_NOTIFY_STATUSMESSAGE_IN_SIM             (0x00000009 | RIL_NCLASS_MESSAGE)  // @constdefine Incoming status-report message stored to SIM; lpData points to RILMESSAGE_IN_SIM

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Network | Network-related notifications (RIL_NCLASS_NETWORK)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_REGSTATUSCHANGED                 (0x00000001 | RIL_NCLASS_NETWORK)  // @constdefine Network registration status has changed; lpData points to the new status (RIL_REGSTAT_* constant)
#define RIL_NOTIFY_CALLMETER                        (0x00000002 | RIL_NCLASS_NETWORK)  // @constdefine Call meter has changed; lpData points to a DWORD containing new current call meter value
#define RIL_NOTIFY_CALLMETERMAXREACHED              (0x00000003 | RIL_NCLASS_NETWORK)  // @constdefine Call meter maximum has been reached; lpData is NULL
#define RIL_NOTIFY_GPRSREGSTATUSCHANGED             (0x00000004 | RIL_NCLASS_NETWORK)  // @constdefine Network registration status has changed; lpData points to the new status (RIL_REGSTAT_* constant)
#define RIL_NOTIFY_SYSTEMCHANGED                    (0x00000005 | RIL_NCLASS_NETWORK)  // @constdefine This indicates that the type of coverage which is available has changed.  Typically one would expect IS-95A or 1xRTT, however CDMA does allow overlay systems; lpData is <t DWORD> of type RIL_SYSTEMTYPE_ flags
#define RIL_NOTIFY_GPRSCONNECTIONSTATUS             (0x00000006 | RIL_NCLASS_NETWORK)  // @constdefine This indicates the pdp context state has changed. lpData points to RILGPRSCONTEXTACTIVATED
#define RIL_NOTIFY_SYSTEMCAPSCHANGED                (0x00000007 | RIL_NCLASS_NETWORK)  // @constdefine This indicates the system capability has changed. lpData points to the new system capability (RIL_SYSTEMCAPS_* constant)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Supplementary Service | Supplementary service notifications (RIL_NCLASS_SUPSERVICE)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_CALLERID                         (0x00000001 | RIL_NCLASS_SUPSERVICE)  // @constdefine Incoming call CallerID information; lpData points to RILREMOTEPARTYINFO
#define RIL_NOTIFY_DIALEDID                         (0x00000002 | RIL_NCLASS_SUPSERVICE)  // @constdefine Initiated call DialedID information; lpData points to RILREMOTEPARTYINFO
#define RIL_NOTIFY_CALLWAITING                      (0x00000003 | RIL_NCLASS_SUPSERVICE)  // @constdefine Call Waiting information; lpData points to RILCALLWAITINGINFO
#define RIL_NOTIFY_SUPSERVICEDATA                   (0x00000004 | RIL_NCLASS_SUPSERVICE)  // @constdefine Ustructured supplementary service data; lpData points to RILSUPSERVICEDATA
#define RIL_NOTIFY_INTERMEDIATESS                   (0x00000005 | RIL_NCLASS_SUPSERVICE)  // @constdefine Ustructured supplementary service data; lpData points to RILINTERMEDIATESSINFO
#define RIL_NOTIFY_UNSOLICITEDSS                    (0x00000006 | RIL_NCLASS_SUPSERVICE)  // @constdefine Ustructured supplementary service data; lpData points to RILUNSOLICITEDSSINFO

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Phonebook | Phonebook notifications (RIL_NCLASS_PHONEBOOK)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_PHONEBOOKENTRYSTORED             (0x00000001 | RIL_NCLASS_PHONEBOOK)  // @constdefine A phonebook entry has been added to storage; lpData points to the storage
                                                                                         // index assigned to the new entry (ifdwIndex is RIL_PBINDEX_FIRSTAVAILABLE, the new entry was stored in the first available location)
#define RIL_NOTIFY_PHONEBOOKENTRYDELETED            (0x00000002 | RIL_NCLASS_PHONEBOOK)  // @constdefine A phonebook entry has been deleted from storage; lpData points to the storage index occupied by the deleted entry
#define RIL_NOTIFY_PHONEBOOKSTORAGECHANGED          (0x00000003 | RIL_NCLASS_PHONEBOOK)  // @constdefine Phonebook storage location has been changed; lpData points to RIL_PBLOC_* constant

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Toolkit | SIM Toolkit notifications (RIL_NCLASS_SIMTOOLKIT)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_SIMTOOLKITCMD                    (0x00000001 | RIL_NCLASS_SIMTOOLKIT)  // @constdefine A SIM Toolkit command was not handled by the radio; lpData points to array of bytes containing the command
#define RIL_NOTIFY_SIMTOOLKITCALLSETUP              (0x00000002 | RIL_NCLASS_SIMTOOLKIT)  // @constdefine SIM Toolkit is trying to set up a call and call conditions were successfully checked by the radio;
                                                                                          // lpData points to a DWORD containing the redial timeout for the call (in milliseconds)
#define RIL_NOTIFY_SIMTOOLKITEVENT                  (0x00000003 | RIL_NCLASS_SIMTOOLKIT)  // @constdefine A SIM Toolkit command was handled by the radio or the radio sent a SIm Toolkit command response to the SIM;
                                                                                          // lpData points to array of bytes containing the command or response sent
#define RIL_NOTIFY_SIMTOOLKITSESSIONEND             (0x00000004 | RIL_NCLASS_SIMTOOLKIT)  // @constdefine A SIM Toolkit command session is ending

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Radio State Change | Radio State Change notifications (RIL_NCLASS_RADIOSTATE)
//
// @comm None
//
// -----------------------------------------------------------------------------

#define RIL_NOTIFY_RADIOEQUIPMENTSTATECHANGED      (0x00000001 | RIL_NCLASS_RADIOSTATE)  // @constdefine Carries a STRUCT (RILEQUIPMENTSTATE) stating The Radio equiptmentstate has changed, also notifies a driver defined Radio ON or OFF state
#define RIL_NOTIFY_RADIOPRESENCECHANGED            (0x00000002 | RIL_NCLASS_RADIOSTATE)  // @constdefine Carries a dword (RIL_RADIOPRESENCE_*) stating that a Radio Module/Driver has been changed (removed, inserted, etc)
#define RIL_NOTIFY_RADIORESET                      (0x00000003 | RIL_NCLASS_RADIOSTATE)  // @constdefine The driver has detected that the radio reset itself. lpData points to NULL

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Misc | Miscellaneous notifications (RIL_NCLASS_MISC)
//
// @comm None
//
// -----------------------------------------------------------------------------

#define RIL_NOTIFY_SIMNOTACCESSIBLE                 (0x00000001 | RIL_NCLASS_MISC)  // @constdefine SIM card has been removed or has failed to respond; lpData is NULL
#define RIL_NOTIFY_DTMFSIGNAL                       (0x00000002 | RIL_NCLASS_MISC)  // @constdefine A DTMF signal has been detected; lpData points to char
#define RIL_NOTIFY_GPRSCLASS_NETWORKCHANGED         (0x00000003 | RIL_NCLASS_MISC)  // @constdefine Network has indicated a change in GPRS class
	                                                                                    // lpData points to a DWORD containing the new RIL_GPRSCLASS_* value
#define RIL_NOTIFY_GPRSCLASS_RADIOCHANGED           (0x00000004 | RIL_NCLASS_MISC)  // @constdefine The radio has indicated a change in GPRS class
	                                                                                    // lpData points to a DWORD containing the new RIL_GPRSCLASS_* value
#define RIL_NOTIFY_SIGNALQUALITY                    (0x00000005 | RIL_NCLASS_MISC)  // @constdefine Signal Quality Notification
	                                                                                    // lpData points to a RILSIGNALQUALITY structure
#define RIL_NOTIFY_MAINTREQUIRED                    (0x00000006 | RIL_NCLASS_MISC)  // @constdefine BS notification that MS requires servicing; lpdata is NULL
#define RIL_NOTIFY_PRIVACYCHANGED                   (0x00000007 | RIL_NCLASS_MISC)  // @constdefine Call Privacy Status; lpData points to DWORD of value RIL_CALLPRIVACY_*
#define RIL_NOTIFY_SIM_DATACHANGE                   (0x00000008 | RIL_NCLASS_MISC)  // @constdefine data change notification; lpData points to DWORD of value RIL_SIMDATACHANGE_*
#define RIL_NOTIFY_ATLOGGING                        (0x00000009 | RIL_NCLASS_MISC)  // @constdefine at command log data present
#define RIL_NOTIFY_SIMSTATUSCHANGED                 (0x00000010 | RIL_NCLASS_MISC)  // @constdefine SIM card state has changed. Carries a DWORD (RIL_SIMSTATUSCHANGED_*) with the current state.
                                                                                        // Notification is sent only when encountering error conditions from the radio.

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification Device Specific | Device Specific notifications (RIL_NCLASS_DEVSPECIFIC)
//
// @comm None
//
// -----------------------------------------------------------------------------

#define RIL_NOTIFY_LOCATION                         (0x00008000 | RIL_NCLASS_DEVSPECIFIC) // @constdefine Location Services; lpData points to DWORD of value RIL_LOCATION_*
#define RIL_NOTIFY_ROAMSTATUS                       (0x00008001 | RIL_NCLASS_DEVSPECIFIC) // @constdefine Roaming Status; lpData points to DWORD of value RIL_ROAMSTATUS_*

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Notification NDIS | Device Specific notifications (RIL_NCLASS_NDIS)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NOTIFY_NDIS_IPCONFIG                    (0x00000001 | RIL_NCLASS_NDIS) // @constdefine IP configuration received and is ready for use.
                                                                                   // lpData points to a RILNDISIPCONFIG structure.
#define RIL_NOTIFY_NDIS_PACKETRECEIVED              (0x00000002 | RIL_NCLASS_NDIS) // @constdefine IP packet available.
                                                                                   // lpData points to a RILNDISPACKET structure.
#define RIL_NOTIFY_NDIS_XFERSTATUSCHANGED           (0x00000003 | RIL_NCLASS_NDIS) // @constdefine packet flow control change occurred.
                                                                                   // lpData ponts to DWORD of [ RIL_NDIS_XON |RIL_NDIS_XOFF ]
//
// Macro to extract notification class from notification code
//
#define NCLASS_FROM_NOTIFICATION(code)              ((code) & 0xffff0000)


//
// Structure parameter flags
//



// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILNDISIPCONFIG
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_PARAM_NDISIPCONFIG_PROTOCOL_IPV4      (0x00000001)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_PROTOCOL_IPV6      (0x00000002)  // @paramdefine
//
// ipv4 defines
#define RIL_PARAM_NDISIPCONFIG_IPADDR             (0x00000001)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_PRIMARYDNS         (0x00000002)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_SECONDARYDNS       (0x00000004)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_DEFAULTGATEWAY     (0x00000008)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_SUBNETMASK         (0x00000010)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_ALL                (0x0000001f)  // @paramdefine
//
// ipv6 defines
#define RIL_PARAM_NDISIPCONFIG_IPV6_IPADDR          (0x00000001)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_PRIMARYDNS      (0x00000002)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_SECONDARYDNS    (0x00000004)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_DEFAULTGATEWAY  (0x00000008)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_SUBNETMASK      (0x00000010)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_FLOWINFO        (0x00000020)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_SCOPEID         (0x00000040)  // @paramdefine
#define RIL_PARAM_NDISIPCONFIG_IPV6_ALL             (0x0000007f)  // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILNDISGPRSCONTEXT
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_PARAM_RILNDISGPRSCONTEXT_USERNAME           (0x00000001)  // @paramdefine
#define RIL_PARAM_RILNDISGPRSCONTEXT_PASSWORD           (0x00000002)  // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILADDRESS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_A_TYPE                            (0x00000001) // @paramdefine
#define RIL_PARAM_A_NUMPLAN                         (0x00000002) // @paramdefine
#define RIL_PARAM_A_ADDRESS                         (0x00000004) // @paramdefine
#define RIL_PARAM_A_ALL                             (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSUBADDRESS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SA_TYPE                           (0x00000001) // @paramdefine
#define RIL_PARAM_SA_SUBADDRESS                     (0x00000002) // @paramdefine
#define RIL_PARAM_SA_ALL                            (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSERIALPORTSTATS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SPS_READBITSPERSECOND             (0x00000001) // @paramdefine
#define RIL_PARAM_SPS_WRITTENBITSPERSECOND          (0x00000002) // @paramdefine
#define RIL_PARAM_SPS_ALL                           (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSUBSCRIBERINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SI_ADDRESS                        (0x00000001) // @paramdefine
#define RIL_PARAM_SI_DESCRIPTION                    (0x00000002) // @paramdefine
#define RIL_PARAM_SI_SPEED                          (0x00000004) // @paramdefine
#define RIL_PARAM_SI_SERVICE                        (0x00000008) // @paramdefine
#define RIL_PARAM_SI_ITC                            (0x00000010) // @paramdefine
#define RIL_PARAM_SI_ADDRESSID                      (0x00000020) // @paramdefine
#define RIL_PARAM_SI_ALL                            (0x0000003f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILOPERATORNAMES
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_ON_LONGNAME                       (0x00000001) // @paramdefine
#define RIL_PARAM_ON_SHORTNAME                      (0x00000002) // @paramdefine
#define RIL_PARAM_ON_NUMNAME                        (0x00000004) // @paramdefine
#define RIL_PARAM_ON_COUNTRY_CODE                   (0x00000008) // @paramdefine
#define RIL_PARAM_ON_ALL                            (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILOPERATORINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_OI_INDEX                          (0x00000001) // @paramdefine
#define RIL_PARAM_OI_STATUS                         (0x00000002) // @paramdefine
#define RIL_PARAM_OI_NAMES                          (0x00000004) // @paramdefine
#define RIL_PARAM_OI_ALL                            (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCALLERIDSETTINGS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CIDS_PROVISIONING                 (0x00000001) // @paramdefine
#define RIL_PARAM_CIDS_STATUS                       (0x00000002) // @paramdefine
#define RIL_PARAM_CIDS_ALL                          (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILHIDEIDSETTINGS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_HIDS_STATUS                       (0x00000001) // @paramdefine
#define RIL_PARAM_HIDS_PROVISIONING                 (0x00000002) // @paramdefine
#define RIL_PARAM_HIDS_ALL                          (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILDIALEDIDSETTINGS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_DIDS_PROVISIONING                 (0x00000001) // @paramdefine
#define RIL_PARAM_DIDS_STATUS                       (0x00000002) // @paramdefine
#define RIL_PARAM_DIDS_ALL                          (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILHIDECONNECTEDIDSETTINGS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_HCIDS_PROVISIONING                (0x00000001) // @paramdefine
#define RIL_PARAM_HCIDS_STATUS                      (0x00000002) // @paramdefine
#define RIL_PARAM_HCIDS_ALL                         (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCLOSEDGROUPSETTINGS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CGS_STATUS                        (0x00000001) // @paramdefine
#define RIL_PARAM_CGS_INDEX                         (0x00000002) // @paramdefine
#define RIL_PARAM_CGS_INFO                          (0x00000004) // @paramdefine
#define RIL_PARAM_CGS_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCALLFORWARDINGSETTINGS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CFS_STATUS                        (0x00000001) // @paramdefine
#define RIL_PARAM_CFS_INFOCLASSES                   (0x00000002) // @paramdefine
#define RIL_PARAM_CFS_ADDRESS                       (0x00000004) // @paramdefine
#define RIL_PARAM_CFS_SUBADDRESS                    (0x00000008) // @paramdefine
#define RIL_PARAM_CFS_DELAYTIME                     (0x00000010) // @paramdefine
#define RIL_PARAM_CFS_ALL                           (0x0000001f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCALLINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CI_ID                             (0x00000001) // @paramdefine
#define RIL_PARAM_CI_DIRECTION                      (0x00000002) // @paramdefine
#define RIL_PARAM_CI_STATUS                         (0x00000004) // @paramdefine
#define RIL_PARAM_CI_TYPE                           (0x00000008) // @paramdefine
#define RIL_PARAM_CI_MULTIPARTY                     (0x00000010) // @paramdefine
#define RIL_PARAM_CI_ADDRESS                        (0x00000020) // @paramdefine
#define RIL_PARAM_CI_DESCRIPTION                    (0x00000040) // @paramdefine
#define RIL_PARAM_CI_CPISTATUS                      (0x00000080) // @paramdefine
#define RIL_PARAM_CI_DISCONNECTCODE           (0x00000100) // @paramdefine
//Note: RIL_PARAM_CI_STATUS and RIL_PARAM_CI_CPISTATUS are mutually exclusive
// parameters because they define how the dwStatus variable is used.
// Therefore, there is no RIL_PARAM_CI_ALL to avoid any ambiguity.

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILGAININFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_GI_TXGAIN                         (0x00000001) // @paramdefine
#define RIL_PARAM_GI_RXGAIN                         (0x00000002) // @paramdefine
#define RIL_PARAM_GI_ALL                            (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILAUDIODEVICEINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_ADI_TXDEVICE                      (0x00000001) // @paramdefine
#define RIL_PARAM_ADI_RXDEVICE                      (0x00000002) // @paramdefine
#define RIL_PARAM_ADI_ALL                           (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILHSCSDINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_HSCSDI_TRANSPRXTIMESLOTS          (0x00000001) // @paramdefine
#define RIL_PARAM_HSCSDI_TRANSPCHANNELCODINGS       (0x00000002) // @paramdefine
#define RIL_PARAM_HSCSDI_NONTRANSPRXTIMESLOTS       (0x00000004) // @paramdefine
#define RIL_PARAM_HSCSDI_NONTRANSPCHANNELCODINGS    (0x00000008) // @paramdefine
#define RIL_PARAM_HSCSDI_AIRINTERFACEUSERRATE       (0x00000010) // @paramdefine
#define RIL_PARAM_HSCSDI_RXTIMESLOTSLIMIT           (0x00000020) // @paramdefine
#define RIL_PARAM_HSCSDI_AUTOSVCLEVELUPGRADING      (0x00000040) // @paramdefine
#define RIL_PARAM_HSCSDI_ALL                        (0x0000007f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCALLHSCSDINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CHSCSDI_RXTIMESLOTS               (0x00000001) // @paramdefine
#define RIL_PARAM_CHSCSDI_TXTIMESLOTS               (0x00000002) // @paramdefine
#define RIL_PARAM_CHSCSDI_AIRINTERFACEUSERRATE      (0x00000004) // @paramdefine
#define RIL_PARAM_CHSCSDI_CHANNELCODING             (0x00000008) // @paramdefine
#define RIL_PARAM_CHSCSDI_ALL                       (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILDATACOMPINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_DCI_DIRECTION                     (0x00000001) // @paramdefine
#define RIL_PARAM_DCI_NEGOTIATION                   (0x00000002) // @paramdefine
#define RIL_PARAM_DCI_MAXDICTENTRIES                (0x00000004) // @paramdefine
#define RIL_PARAM_DCI_MAXSTRING                     (0x00000008) // @paramdefine
#define RIL_PARAM_DCI_ALL                           (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILERRORCORRECTIONINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_ECI_ORIGINALREQUEST               (0x00000001) // @paramdefine
#define RIL_PARAM_ECI_ORIGINALFALLBACK              (0x00000002) // @paramdefine
#define RIL_PARAM_ECI_ANSWERERFALLBACK              (0x00000004) // @paramdefine
#define RIL_PARAM_ECI_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILBEARERSVCINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_BSI_SPEED                         (0x00000001) // @paramdefine
#define RIL_PARAM_BSI_SERVICENAME                   (0x00000002) // @paramdefine
#define RIL_PARAM_BSI_CONNECTIONELEMENT             (0x00000004) // @paramdefine
#define RIL_PARAM_BSI_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILRLPINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_RLPI_IWS                          (0x00000001) // @paramdefine
#define RIL_PARAM_RLPI_MWS                          (0x00000002) // @paramdefine
#define RIL_PARAM_RLPI_ACKTIMER                     (0x00000004) // @paramdefine
#define RIL_PARAM_RLPI_RETRANSMISSIONATTEMPTS       (0x00000008) // @paramdefine
#define RIL_PARAM_RLPI_VERSION                      (0x00000010) // @paramdefine
#define RIL_PARAM_RPLI_RESEQUENCINGPERIOD           (0x00000020) // @paramdefine
#define RIL_PARAM_RPLI_ALL                          (0x0000003f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMSGSERVICEINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_MSI_SERVICE                       (0x00000001) // @paramdefine
#define RIL_PARAM_MSI_MSGCLASSES                    (0x00000002) // @paramdefine
#define RIL_PARAM_MSI_READLOCATION                  (0x00000004) // @paramdefine
#define RIL_PARAM_MSI_READUSED                      (0x00000008) // @paramdefine
#define RIL_PARAM_MSI_READTOTAL                     (0x00000010) // @paramdefine
#define RIL_PARAM_MSI_WRITELOCATION                 (0x00000020) // @paramdefine
#define RIL_PARAM_MSI_WRITEUSED                     (0x00000040) // @paramdefine
#define RIL_PARAM_MSI_WRITETOTAL                    (0x00000080) // @paramdefine
#define RIL_PARAM_MSI_STORELOCATION                 (0x00000100) // @paramdefine
#define RIL_PARAM_MSI_STOREUSED                     (0x00000200) // @paramdefine
#define RIL_PARAM_MSI_STORETOTAL                    (0x00000400) // @paramdefine
#define RIL_PARAM_MSI_ALL                           (0x000007ff) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMSGDCS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_MDCS_TYPE                         (0x00000001) // @paramdefine
#define RIL_PARAM_MDCS_FLAGS                        (0x00000002) // @paramdefine
#define RIL_PARAM_MDCS_MSGCLASS                     (0x00000004) // @paramdefine
#define RIL_PARAM_MDCS_ALPHABET                     (0x00000008) // @paramdefine
#define RIL_PARAM_MDCS_INDICATION                   (0x00000010) // @paramdefine
#define RIL_PARAM_MDCS_LANGUAGE                     (0x00000020) // @paramdefine
#define RIL_PARAM_MDCS_ALL                          (0x0000003f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMSGCONFIG
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_MC_SVCCTRADDRESS                  (0x00000001) // @paramdefine
#define RIL_PARAM_MC_ALL                            (0x00000001) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCBMSGCONFIG
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CBMC_BROADCASTMSGIDS              (0x00000001) // @paramdefine
#define RIL_PARAM_CBMC_BROADCASTMSGLANGS            (0x00000002) // @paramdefine
#define RIL_PARAM_CBMC_ACCEPTIDS                    (0x00000004) // @paramdefine
#define RIL_PARAM_CBMC_ALL                          (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMESSAGE
//
// @comm None
//
// -----------------------------------------------------------------------------
/* List of Unions Labeled

GSM
ID=RIL_MSGTYPE_IN_DELIVER
ISt=RIL_MSGTYPE_IN_STATUS
OS=RIL_MSGTYPE_OUT_SUBMIT
OC=RIL_MSGTYPE_OUT_COMMAND
OR=RIL_MSGTYPE_OUT_RAW
BC=RIL_MSGTYPE_BC_GENERAL

CDMA
ID=RIL_MSGTYPE_IN_IS637DELIVER
ISt=RIL_MSGTYPE_IN_IS637STATUS
OS=RIL_MSGTYPE_OUT_IS637SUBMIT
OSt=RIL_MSGTYPE_OUT_IS637STATUS
*/
// -------This block is the GSM Params for RILMESSAGE (These values may have been recycled;
//        U = This value for the field has been reused in CDMA, and if the RILMESSAGE structure
//            is expanded, developer must careful not to use two recycled fields in the same union.)
#define RIL_PARAM_M_SVCCTRADDRESS                   (0x00000001) // @paramdefine GSM=[ID,ISt,OS,OC,OR,BC] CDMA=[ID,ISt,OS,OSt]
#define RIL_PARAM_M_TYPE                            (0x00000002) // @paramdefine GSM=[ID,ISt,OS,OC,OR,BC] CDMA=[ID,ISt,OS,OSt]
#define RIL_PARAM_M_FLAGS                           (0x00000004) // @paramdefine GSM=[ID,ISt,OS,OC,OR,BC] CDMA=[ID,ISt,OS,OSt]
#define RIL_PARAM_M_ORIGADDRESS                     (0x00000008) // @paramdefine GSM=[ID] CDMA=[ID,ISt]
#define RIL_PARAM_M_TGTRECIPADDRESS                 (0x00000010) // @paramdefine U GSM=[ISt]
#define RIL_PARAM_M_DESTADDRESS                     (0x00000020) // @paramdefine GSM=[OS,OC] CDMA=[OSt,OS]
#define RIL_PARAM_M_SCRECEIVETIME                   (0x00000040) // @paramdefine GSM=[ID] CDMA=[ID,Ist]
#define RIL_PARAM_M_TGTSCRECEIVETIME                (0x00000080) // @paramdefine U GSM=[ISt]
#define RIL_PARAM_M_TGTDISCHARGETIME                (0x00000100) // @paramdefine U GSM=[ISt]
#define RIL_PARAM_M_PROTOCOLID                      (0x00000200) // @paramdefine U GSM=[ISt]
#define RIL_PARAM_M_DATACODING                      (0x00000800) // @paramdefine U GSM=[ID,ISt,OS,BC]
#define RIL_PARAM_M_TGTDLVSTATUS                    (0x00001000) // @paramdefine U GSM=[ISt]
#define RIL_PARAM_M_TGTMSGREFERENCE                 (0x00002000) // @paramdefine U GSM=[OC]
#define RIL_PARAM_M_VPFORMAT                        (0x00004000) // @paramdefine U GSM=[OS]
#define RIL_PARAM_M_VP                              (0x00008000) // @paramdefine U GSM=[OS]
#define RIL_PARAM_M_COMMANDTYPE                     (0x00010000) // @paramdefine U GSM=[OC]
#define RIL_PARAM_M_GEOSCOPE                        (0x00020000) // @paramdefine U GSM=[BC]
#define RIL_PARAM_M_MSGCODE                         (0x00040000) // @paramdefine U GSM=[BC]
#define RIL_PARAM_M_UPDATENUMBER                    (0x00080000) // @paramdefine U GSM=[BC]
#define RIL_PARAM_M_ID                              (0x00100000) // @paramdefine U GSM=[BC]
#define RIL_PARAM_M_TOTALPAGES                      (0x00200000) // @paramdefine U GSM=[BC]
#define RIL_PARAM_M_PAGENUMBER                      (0x00400000) // @paramdefine U GSM=[BC]
#define RIL_PARAM_M_HDRLENGTH                       (0x00800000) // @paramdefine U GSM=[ID,ISt,OS]
#define RIL_PARAM_M_MSGLENGTH                       (0x01000000) // @paramdefine GSM=[ID,ISt,OS,OR,BC] CDMA=[ID,ISt,OS,OSt]
#define RIL_PARAM_M_CMDLENGTH                       (0x02000000) // @paramdefine GSM=[OC]
#define RIL_PARAM_M_HDR                             (0x04000000) // @paramdefine GSM=[ID,ISt,OS]
#define RIL_PARAM_M_MSG                             (0x08000000) // @paramdefine GSM=[ID,ISt,OS,OR,BC] CDMA=[ID,ISt,OS,OSt]
#define RIL_PARAM_M_CMD                             (0x10000000) // @paramdefine U GSM=[OC]

// CDMA Message parameter definitions
#define RIL_PARAM_M_MSGID                           (0x20000000) // @paramdefine CDMA=[ID,ISt,OS,OSt]

#define RIL_PARAM_M_ORIGSUBADDRESS                  (0x40000000) // @paramdefine CDMA=[ID,ISt]
#define RIL_PARAM_M_DESTSUBADDRESS                  (0x80000000) // @paramdefine CDMA=[OS,OSt]
#define RIL_PARAM_M_DIGIT                           (0x00010000) // @paramdefine CDMA=[OS,OSt]

#define RIL_PARAM_M_PRIVACY                         (0x00000100) // @paramdefine CDMA=[ID,OS]
#define RIL_PARAM_M_PRIORITY                        (0x00000200) // @paramdefine CDMA=[ID,OS]
#define RIL_PARAM_M_TELESERVICE                     (0x00000400) // @paramdefine CDMA=[ID,OS]
#define RIL_PARAM_M_LANG                            (0x00000800) // @paramdefine CDMA=[ID,ISt,OS,OSt]

#define RIL_PARAM_M_VALIDITYPERIODABS               (0x00001000) // @paramdefine CDMA=[ID,OS]
#define RIL_PARAM_M_VALIDITYPERIODREL               (0x00002000) // @paramdefine CDMA=[OS]
#define RIL_PARAM_M_DEFERREDDELTIMEABS              (0x00004000) // @paramdefine CDMA=[OS]
#define RIL_PARAM_M_DEFERREDDELTIMEREL              (0x00008000) // @paramdefine CDMA=[OS]

#define RIL_PARAM_M_ENCODING                        (0x00020000) // @paramdefine CDMA=[ID,ISt,OS,OSt]
#define RIL_PARAM_M_USERRESPONSECODE                (0x00040000) // @paramdefine CDMA=[ISt,OSt]
#define RIL_PARAM_M_DISPLAYMODE                     (0x00080000) // @paramdefine CDMA=[ID,OS]

#define RIL_PARAM_M_CALLBACKNUM                     (0x00000010) // @paramdefine CDMA=[ID,OS]
#define RIL_PARAM_M_NUMMSGS                         (0x00000080) // @paramdefine CDMA=[ID]

#define RIL_PARAM_M_CAUSECODE                       (0x00100000) // @paramdefine CDMA=[ISt]
#define RIL_PARAM_M_REPLYSEQNUMBER                  (0x00200000) // @paramdefine CDMA=[ISt,OSt]

#define RIL_PARAM_M_BEARERREPLYACK                  (0x00200000) // @paramdefine CDMA=[OS]
#define RIL_PARAM_M_USERACK                         (0x00400000) // @paramdefine CDMA=[ID,OS]
#define RIL_PARAM_M_DELIVERYACK                     (0x00800000) // @paramdefine CDMA=[OS]
#define RIL_PARAM_M_MSGSTATUSTYPE                   (0x10000000) // @paramdefine CDMA=[ISt]

#define RIL_PARAM_M_ALL_IN_DELIVER                  (RIL_PARAM_M_TYPE | RIL_PARAM_M_FLAGS | RIL_PARAM_M_ORIGADDRESS | \
                                                     RIL_PARAM_M_PROTOCOLID | RIL_PARAM_M_DATACODING | \
                                                     RIL_PARAM_M_SCRECEIVETIME | RIL_PARAM_M_HDRLENGTH | RIL_PARAM_M_MSGLENGTH | \
                                                     RIL_PARAM_M_HDR | RIL_PARAM_M_MSG)                                  // @paramdefine

#define RIL_PARAM_M_ALL_IN_STATUS                   (RIL_PARAM_M_TYPE | RIL_PARAM_M_FLAGS | RIL_PARAM_M_TGTMSGREFERENCE | \
                                                     RIL_PARAM_M_TGTRECIPADDRESS | RIL_PARAM_M_TGTSCRECEIVETIME | \
                                                     RIL_PARAM_M_TGTDISCHARGETIME | RIL_PARAM_M_TGTDLVSTATUS | \
                                                     RIL_PARAM_M_PROTOCOLID | RIL_PARAM_M_DATACODING | RIL_PARAM_M_HDRLENGTH | \
                                                     RIL_PARAM_M_MSGLENGTH | RIL_PARAM_M_HDR | RIL_PARAM_M_MSG)          // @paramdefine

#define RIL_PARAM_M_ALL_OUT_SUBMIT                  (RIL_PARAM_M_TYPE | RIL_PARAM_M_FLAGS | RIL_PARAM_M_DESTADDRESS | \
                                                     RIL_PARAM_M_PROTOCOLID | RIL_PARAM_M_DATACODING | RIL_PARAM_M_VPFORMAT | \
                                                     RIL_PARAM_M_VP | RIL_PARAM_M_HDRLENGTH | RIL_PARAM_M_MSGLENGTH | \
                                                     RIL_PARAM_M_HDR | RIL_PARAM_M_MSG)                                  // @paramdefine

#define RIL_PARAM_M_ALL_OUT_COMMAND                 (RIL_PARAM_M_TYPE | RIL_PARAM_M_FLAGS | RIL_PARAM_M_PROTOCOLID | \
                                                     RIL_PARAM_M_COMMANDTYPE | RIL_PARAM_M_TGTMSGREFERENCE | \
                                                     RIL_PARAM_M_DESTADDRESS | RIL_PARAM_M_CMDLENGTH | RIL_PARAM_M_CMD)  // @paramdefine

#define RIL_PARAM_M_ALL_BC_GENERAL                  (RIL_PARAM_M_TYPE | RIL_PARAM_M_GEOSCOPE | RIL_PARAM_M_MSGCODE | \
                                                     RIL_PARAM_M_UPDATENUMBER | RIL_PARAM_M_ID | RIL_PARAM_M_DATACODING | \
                                                     RIL_PARAM_M_TOTALPAGES | RIL_PARAM_M_PAGENUMBER | RIL_PARAM_M_MSGLENGTH | \
                                                     RIL_PARAM_M_MSG)                                                    // @paramdefine

#define RIL_PARAM_M_ALL_OUT_RAW                     (RIL_PARAM_M_TYPE | RIL_PARAM_M_FLAGS | \
                                                     RIL_PARAM_M_MSGLENGTH | RIL_PARAM_M_MSG)                            // @paramdefine

#define RIL_PARAM_M_ALL_IN_IS637DELIVER             (RIL_PARAM_M_TYPE | RIL_PARAM_M_MSGID | RIL_PARAM_M_TELESERVICE | \
                                                     RIL_PARAM_M_DISPLAYMODE | RIL_PARAM_M_USERACK | RIL_PARAM_M_ORIGADDRESS | \
                                                     RIL_PARAM_M_ORIGSUBADDRESS | RIL_PARAM_M_SCRECEIVETIME | RIL_PARAM_M_PRIORITY | \
                                                     RIL_PARAM_M_PRIVACY | RIL_PARAM_M_CALLBACKNUM | RIL_PARAM_M_NUMMSGS | \
                                                     RIL_PARAM_M_VALIDITYPERIODABS | RIL_PARAM_M_LANG | RIL_PARAM_M_ENCODING | \
                                                     RIL_PARAM_M_MSGLENGTH | RIL_PARAM_M_MSG)                           // @paramdefine

#define RIL_PARAM_M_ALL_OUT_IS637SUBMIT             (RIL_PARAM_M_TYPE | RIL_PARAM_M_MSGID | RIL_PARAM_M_TELESERVICE | \
                                                     RIL_PARAM_M_DISPLAYMODE | RIL_PARAM_M_DESTADDRESS | RIL_PARAM_M_DESTSUBADDRESS | \
                                                     RIL_PARAM_M_DIGIT | RIL_PARAM_M_BEARERREPLYACK | RIL_PARAM_M_PRIORITY | \
                                                     RIL_PARAM_M_PRIVACY | RIL_PARAM_M_CALLBACKNUM | RIL_PARAM_M_USERACK | \
                                                     RIL_PARAM_M_DELIVERYACK | RIL_PARAM_M_VALIDITYPERIODABS | \
                                                     RIL_PARAM_M_VALIDITYPERIODREL | RIL_PARAM_M_DEFERREDDELTIMEABS | \
                                                     RIL_PARAM_M_DEFERREDDELTIMEREL | RIL_PARAM_M_LANG | RIL_PARAM_M_ENCODING | \
                                                     RIL_PARAM_M_MSGLENGTH | RIL_PARAM_M_MSG)                           // @paramdefine

#define RIL_PARAM_M_ALL_IN_IS637STATUS              (RIL_PARAM_M_TYPE | RIL_PARAM_M_MSGID | RIL_PARAM_M_ORIGADDRESS | \
                                                     RIL_PARAM_M_ORIGSUBADDRESS | RIL_PARAM_M_SCRECEIVETIME | RIL_PARAM_M_CAUSECODE | \
                                                     RIL_PARAM_M_REPLYSEQNUMBER | RIL_PARAM_M_LANG | RIL_PARAM_M_ENCODING | \
                                                     RIL_PARAM_M_MSGLENGTH | RIL_PARAM_M_MSG | RIL_PARAM_M_USERRESPONSECODE | \
                                                     RIL_PARAM_M_MSGSTATUSTYPE)                                         // @paramdefine

#define RIL_PARAM_M_ALL_OUT_IS637STATUS             (RIL_PARAM_M_TYPE | RIL_PARAM_M_MSGID |  RIL_PARAM_M_DESTADDRESS | \
                                                     RIL_PARAM_M_DESTSUBADDRESS | RIL_PARAM_M_REPLYSEQNUMBER | RIL_PARAM_M_LANG | \
                                                     RIL_PARAM_M_ENCODING | RIL_PARAM_M_MSGLENGTH | RIL_PARAM_M_MSG | \
                                                     RIL_PARAM_M_USERRESPONSECODE | RIL_PARAM_M_DIGIT)                  // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMESSAGE_IN_SIM
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_MIS_LOCATION                      0x00000001 // @paramdefine
#define RIL_PARAM_MIS_INDEX                         0x00000002 // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMESSAGEINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_MI_INDEX                          (0x00000001) // @paramdefine
#define RIL_PARAM_MI_STATUS                         (0x00000002) // @paramdefine
#define RIL_PARAM_MI_MESSAGE                        (0x00000004) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILEQUIPMENTINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_EI_MANUFACTURER                   (0x00000001) // @paramdefine
#define RIL_PARAM_EI_MODEL                          (0x00000002) // @paramdefine
#define RIL_PARAM_EI_REVISION                       (0x00000004) // @paramdefine
#define RIL_PARAM_EI_SERIALNUMBER                   (0x00000008) // @paramdefine
#define RIL_PARAM_EI_ALL                            (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILPHONEBOOKINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_PBI_STORELOCATION                 (0x00000001) // @paramdefine
#define RIL_PARAM_PBI_USED                          (0x00000002) // @paramdefine
#define RIL_PARAM_PBI_TOTAL                         (0x00000004) // @paramdefine
#define RIL_PARAM_PBI_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILPHONEBOOKENTRY
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_PBE_INDEX                         (0x00000001) // @paramdefine
#define RIL_PARAM_PBE_ADDRESS                       (0x00000002) // @paramdefine
#define RIL_PARAM_PBE_TEXT                          (0x00000004) // @paramdefine
#define RIL_PARAM_PBE_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILATRINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_ATR_PHASE                         (0x00000001)  // @paramdefine
#define RIL_PARAM_ATR_SIZE                          (0x00000002)  // @paramdefine
#define RIL_PARAM_ATR_ATR                           (0x00000004)  // @paramdefine
#define RIL_PARAM_ATR_ALL                           (0x00000007)  // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMTOOLKITNOTIFYCAPS
//
// @comm Parameters for LPRILSIMTOOLKITNOTIFYCAPS -> dwParams
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SIMTKN_REFRESH                    (0x00000001) // @paramdefine
#define RIL_PARAM_SIMTKN_MORETIME                   (0x00000002) // @paramdefine
#define RIL_PARAM_SIMTKN_POLLINTERVAL               (0x00000004) // @paramdefine
#define RIL_PARAM_SIMTKN_POLLINGOFF                 (0x00000008) // @paramdefine
#define RIL_PARAM_SIMTKN_SETUPCALL                  (0x00000010) // @paramdefine
#define RIL_PARAM_SIMTKN_SENDSS                     (0x00000020) // @paramdefine
#define RIL_PARAM_SIMTKN_SENDSMS                    (0x00000040) // @paramdefine
#define RIL_PARAM_SIMTKN_PLAYTONE                   (0x00000080) // @paramdefine
#define RIL_PARAM_SIMTKN_DISPLAYTEXT                (0x00000100) // @paramdefine
#define RIL_PARAM_SIMTKN_GETINKEY                   (0x00000200) // @paramdefine
#define RIL_PARAM_SIMTKN_GETINPUT                   (0x00000400) // @paramdefine
#define RIL_PARAM_SIMTKN_SELECTITEM                 (0x00000800) // @paramdefine
#define RIL_PARAM_SIMTKN_SETUPMENU                  (0x00001000) // @paramdefine
#define RIL_PARAM_SIMTKN_LOCALINFO                  (0x00002000) // @paramdefine
#define RIL_PARAM_SIMTKN_NOTIFYFLAGS                (0x00004000) // @paramdefine
#define RIL_PARAM_SIMTKN_SENDUSSD                   (0x00008000) // @paramdefine
#define RIL_PARAM_SIMTKN_SETUPIDLEMODETEXT          (0x00010000) // @paramdefine
#define RIL_PARAM_SIMTKN_SETUPEVENTLIST             (0x00020000) // @paramdefine
#define RIL_PARAM_SIMTKN_SENDDTMF                   (0x00040000) // @paramdefine
#define RIL_PARAM_SIMTKN_LAUNCHBROWSER              (0x00080000) // @paramdefine
#define RIL_PARAM_SIMTKN_OPENCHANNEL                (0x00100000) // @paramdefine
#define RIL_PARAM_SIMTKN_CLOSECHANNEL               (0x00200000) // @paramdefine
#define RIL_PARAM_SIMTKN_RECEIVEDATA                (0x00400000) // @paramdefine
#define RIL_PARAM_SIMTKN_SENDDATA                   (0x00800000) // @paramdefine
#define RIL_PARAM_SIMTKN_TIMERMANAGEMENT            (0x01000000) // @paramdefine
#define RIL_PARAM_SIMTKN_EVENTS                     (0x02000000) // @paramdefine
#define RIL_PARAM_SIMTKN_RUNATCMD                   (0x04000000) // @paramdefine
#define RIL_PARAM_SIMTKN_ALL                        (0x07ffffff) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMTOOLKITEVENTCAPS
//
// @comm Parameters for LPRILSIMTOOLKITEVENTCAPS -> dwParams
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SIMTKE_MTCALL                     (0x00000001) // @paramdefine
#define RIL_PARAM_SIMTKE_CALLCONNECTED              (0x00000002) // @paramdefine
#define RIL_PARAM_SIMTKE_CALLDISCONNECTED           (0x00000004) // @paramdefine
#define RIL_PARAM_SIMTKE_LOCATIONSTATUS             (0x00000008) // @paramdefine
#define RIL_PARAM_SIMTKE_USERACTIVITY               (0x00000010) // @paramdefine
#define RIL_PARAM_SIMTKE_IDLESCREEN                 (0x00000020) // @paramdefine
#define RIL_PARAM_SIMTKE_LANGUAGESELECTION          (0x00000040) // @paramdefine
#define RIL_PARAM_SIMTKE_BROWSERTERMINATION         (0x00000080) // @paramdefine
#define RIL_PARAM_SIMTKE_DATAAVAILABLE              (0x00000100) // @paramdefine
#define RIL_PARAM_SIMTKE_CHANNELSTATUS              (0x00000200) // @paramdefine
#define RIL_PARAM_SIMTKE_DISPLAYCHANGE              (0x00000400) // @paramdefine
#define RIL_PARAM_SIMTKE_ALL                        (0x000007FF) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMTOOLKITCMD
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SIMTKIT_CMD_ID                    (0x00000001) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_TAG                   (0x00000002) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_TYPE                  (0x00000004) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_QUALIFIER             (0x00000008) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_ERROR                 (0x00000010) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_DETAILS_OFFSET        (0x00000020) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_DETAILS_SIZE          (0x00000040) // @paramdefine
#define RIL_PARAM_SIMTKIT_CMD_ALL                   (0x0000007F) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMTOOLKITRSP
//
// @comm None
//
// -----------------------------------------------------------------------------

#define RIL_PARAM_SIMTKIT_RSP_ID                    (0x00000001) // @paramdefine
#define RIL_PARAM_SIMTKIT_RSP_TAG                   (0x00000002) // @paramdefine
#define RIL_PARAM_SIMTKIT_RSP_TYPE                  (0x00000004) // @paramdefine
#define RIL_PARAM_SIMTKIT_RSP_QUALIFIER             (0x00000008) // @paramdefine
#define RIL_PARAM_SIMTKIT_RSP_RESPONSE              (0x00000010) // @paramdefine
#define RIL_PARAM_SIMTKIT_RSP_ADDITIONALINFO        (0x00000020) // @paramdefine
#define RIL_PARAM_SIMTKIT_RSP_ALL                   (0x0000003F) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMCMDPARAMETERS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SCP_FILEID                        (0x00000001) // @paramdefine
#define RIL_PARAM_SCP_PARAM1                        (0x00000002) // @paramdefine
#define RIL_PARAM_SCP_PARAM2                        (0x00000004) // @paramdefine
#define RIL_PARAM_SCP_PARAM3                        (0x00000008) // @paramdefine
#define RIL_PARAM_SCP_ALL                           (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMRESPONSE
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SR_STATUSWORD1                    (0x00000001) // @paramdefine
#define RIL_PARAM_SR_STATUSWORD2                    (0x00000002) // @paramdefine
#define RIL_PARAM_SR_RESPONSE                       (0x00000004) // @paramdefine
#define RIL_PARAM_SR_ALL                            (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMRECORDSTATUS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SRS_RECORDTYPE                    (0x00000001)     // @paramdefine
#define RIL_PARAM_SRS_ITEMCOUNT                     (0x00000002)     // @paramdefine
#define RIL_PARAM_SRS_SIZE                          (0x00000004)     // @paramdefine
#define RIL_PARAM_SRS_ALL                           (0x00000007)     // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCOSTINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CSTI_CCM                          (0x00000001) // @paramdefine
#define RIL_PARAM_CSTI_ACM                          (0x00000002) // @paramdefine
#define RIL_PARAM_CSTI_MAXACM                       (0x00000004) // @paramdefine
#define RIL_PARAM_CSTI_COSTPERUNIT                  (0x00000008) // @paramdefine
#define RIL_PARAM_CSTI_CURRENCY                     (0x00000010) // @paramdefine
#define RIL_PARAM_CSTI_ALL                          (0x0000001f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIGNALQUALITY
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SQ_SIGNALSTRENGTH                 (0x00000001) // @paramdefine
#define RIL_PARAM_SQ_MINSIGNALSTRENGTH              (0x00000002) // @paramdefine
#define RIL_PARAM_SQ_MAXSIGNALSTRENGTH              (0x00000004) // @paramdefine
#define RIL_PARAM_SQ_BITERRORRATE                   (0x00000008) // @paramdefine
#define RIL_PARAM_SQ_LOWSIGNALSTRENGTH              (0x00000010) // @paramdefine
#define RIL_PARAM_SQ_HIGHSIGNALSTRENGTH             (0x00000020) // @paramdefine
#define RIL_PARAM_SQ_ALL                            (0x0000003f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCELLTOWERINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CTI_MOBILECOUNTRYCODE             (0x00000001) // @paramdefine
#define RIL_PARAM_CTI_MOBILENETWORKCODE             (0x00000002) // @paramdefine
#define RIL_PARAM_CTI_LOCATIONAREACODE              (0x00000004) // @paramdefine
#define RIL_PARAM_CTI_CELLID                        (0x00000008) // @paramdefine
#define RIL_PARAM_CTI_BASESTATIONID                 (0x00000010) // @paramdefine
#define RIL_PARAM_CTI_BROADCASTCONTROLCHANNEL       (0x00000020) // @paramdefine
#define RIL_PARAM_CTI_RXLEVEL                       (0x00000040) // @paramdefine
#define RIL_PARAM_CTI_RXLEVELFULL                   (0x00000080) // @paramdefine
#define RIL_PARAM_CTI_RXLEVELSUB                    (0x00000100) // @paramdefine
#define RIL_PARAM_CTI_RXQUALITY                     (0x00000200) // @paramdefine
#define RIL_PARAM_CTI_RXQUALITYFULL                 (0x00000400) // @paramdefine
#define RIL_PARAM_CTI_RXQUALITYSUB                  (0x00000800) // @paramdefine
#define RIL_PARAM_CTI_IDLETIMESLOT                  (0x00001000) // @paramdefine
#define RIL_PARAM_CTI_TIMINGADVANCE                 (0x00002000) // @paramdefine
#define RIL_PARAM_CTI_GPRSCELLID                    (0x00004000) // @paramdefine
#define RIL_PARAM_CTI_GPRSBASESTATIONID             (0x00008000) // @paramdefine
#define RIL_PARAM_CTI_NUMBCCH                       (0x00010000) // @paramdefine
#define RIL_PARAM_CTI_NMR                           (0x00020000) // @paramdefine
#define RIL_PARAM_CTI_BCCH                          (0x00040000) // @paramdefine
#define RIL_PARAM_CTI_ALL                           (0x0007ffff) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILEQUIPMENTSTATE
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_EQUIPMENTSTATE_RADIOSUPPORT        (0x00000001) // @paramdefine
#define RIL_PARAM_EQUIPMENTSTATE_EQSTATE             (0x00000002) // @paramdefine
#define RIL_PARAM_EQUIPMENTSTATE_READYSTATE          (0x00000004) // @paramdefine
#define RIL_PARAM_EQUIPMENTSTATE_ALL                 (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILREMOTEPARTYINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_RPI_ADDRESS                       (0x00000001) // @paramdefine
#define RIL_PARAM_RPI_SUBADDRESS                    (0x00000002) // @paramdefine
#define RIL_PARAM_RPI_DESCRIPTION                   (0x00000004) // @paramdefine
#define RIL_PARAM_RPI_VALIDITY                      (0x00000008) // @paramdefine
#define RIL_PARAM_RPI_ALL                           (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCALLWAITINGINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CWI_CALLTYPE                      (0x00000001) // @paramdefine
#define RIL_PARAM_CWI_CALLERINFO                    (0x00000002) // @paramdefine
#define RIL_PARAM_CWI_ADDRESSID                     (0x00000004) // @paramdefine
#define RIL_PARAM_CWI_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILINTERMEDIATESSINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_INTSS_NOTIFICATIONCODE            (0x00000001) // @paramdefine
#define RIL_PARAM_INTSS_CUGINDEX                    (0x00000002) // @paramdefine
#define RIL_PARAM_INTSS_ALL                         (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILUNSOLICITEDSSINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_UNSSS_NOTIFICATIONCODE            (0x00000001) // @paramdefine
#define RIL_PARAM_UNSSS_CUGINDEX                    (0x00000002) // @paramdefine
#define RIL_PARAM_UNSSS_ADDRESS                     (0x00000004) // @paramdefine
#define RIL_PARAM_UNSSS_SUBADDR                     (0x00000008) // @paramdefine
#define RIL_PARAM_UNSSS_ALL                         (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILRINGINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_RI_CALLTYPE                       (0x00000001) // @paramdefine
#define RIL_PARAM_RI_SERVICEINFO                    (0x00000002) // @paramdefine
#define RIL_PARAM_RI_ADDRESSID                      (0x00000004) // @paramdefine
#define RIL_PARAM_RI_ALL                            (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILDIALINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_DI_CMDID                          (0x00000001) // @paramdefine
#define RIL_PARAM_DI_CALLID                         (0x00000002) // @paramdefine
#define RIL_PARAM_DI_ALL                            (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCONNECTINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CNI_CALLTYPE                      (0x00000001) // @paramdefine
#define RIL_PARAM_CNI_BAUDRATE                      (0x00000002) // @paramdefine
#define RIL_PARAM_CNI_ALL                           (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSERVICEINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SVCI_SYNCHRONOUS                  (0x00000001) // @paramdefine
#define RIL_PARAM_SVCI_TRANSPARENT                  (0x00000002) // @paramdefine
#define RIL_PARAM_SVCI_ALL                          (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILMSGSTORAGEINFO
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_MSTI_READLOCATION                 (0x00000001) // @paramdefine
#define RIL_PARAM_MSTI_WRITELOCATION                (0x00000002) // @paramdefine
#define RIL_PARAM_MSTI_STORELOCATION                (0x00000004) // @paramdefine
#define RIL_PARAM_MSTI_ALL                          (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSUPSERVICEDATA
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_SSDI_STATUS                       (0x00000001) // @paramdefine
#define RIL_PARAM_SSDI_DATA                         (0x00000002) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSDIAL
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CD_CALLTYPES                      (0x00000001) // @paramdefine
#define RIL_PARAM_CD_OPTIONS                        (0x00000002) // @paramdefine
#define RIL_PARAM_CD_ALL                            (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSBEARERSVC
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CBS_SPEEDS1                       (0x00000001) // @paramdefine
#define RIL_PARAM_CBS_SPEEDS2                       (0x00000002) // @paramdefine
#define RIL_PARAM_CBS_SERVICENAMES                  (0x00000004) // @paramdefine
#define RIL_PARAM_CBS_CONNECTIONELEMENTS            (0x00000008) // @paramdefine
#define RIL_PARAM_CBS_ALL                           (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSRLP
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CRLP_VERSION                      (0x00000001) // @paramdefine
#define RIL_PARAM_CRLP_IWSRANGE                     (0x00000002) // @paramdefine
#define RIL_PARAM_CRLP_MWSRANGE                     (0x00000004) // @paramdefine
#define RIL_PARAM_CRLP_ACKTIMERRANGE                (0x00000008) // @paramdefine
#define RIL_PARAM_CRLP_RETRANSMISSIONATTSRANGE      (0x00000010) // @paramdefine
#define RIL_PARAM_CRLP_RESEQPERIODRANGE             (0x00000020) // @paramdefine
#define RIL_PARAM_CRLP_ALL                          (0x0000003f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSMSGMEMORYLOCATIONS
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CMML_READLOCATIONS                (0x00000001) // @paramdefine
#define RIL_PARAM_CMML_WRITELOCATIONS               (0x00000002) // @paramdefine
#define RIL_PARAM_CMML_STORELOCATIONS               (0x00000004) // @paramdefine
#define RIL_PARAM_CMML_ALL                          (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSLOCKINGPWDLENGTH
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CLPL_FACILITY                     (0x00000001) // @paramdefine
#define RIL_PARAM_CLPL_PASSWORDLENGTH               (0x00000002) // @paramdefine
#define RIL_PARAM_CLPL_ALL                          (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSBARRINGPWDLENGTH
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CBPL_TYPE                         (0x00000001) // @paramdefine
#define RIL_PARAM_CBPL_PASSWORDLENGTH               (0x00000002) // @paramdefine
#define RIL_PARAM_CBPL_ALL                          (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSDATACOMPRESSION
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CDC_DIRECTION                     (0x00000001) // @paramdefine
#define RIL_PARAM_CDC_NEGOTIATION                   (0x00000002) // @paramdefine
#define RIL_PARAM_CDC_MAXDICT                       (0x00000004) // @paramdefine
#define RIL_PARAM_CDC_MAXSTRING                     (0x00000008) // @paramdefine
#define RIL_PARAM_CDC_ALL                           (0x0000000f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSERRORCORRECTION
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CEC_ORIGINALREQUEST               (0x00000001) // @paramdefine
#define RIL_PARAM_CEC_ORIGINALFALLBACK              (0x00000002) // @paramdefine
#define RIL_PARAM_CEC_ANSWERERFALLBACK              (0x00000004) // @paramdefine
#define RIL_PARAM_CEC_ALL                           (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSHSCSD
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CHSCSD_MULTISLOTCLASS             (0x00000001) // @paramdefine
#define RIL_PARAM_CHSCSD_MAXRXTIMESLOTS             (0x00000002) // @paramdefine
#define RIL_PARAM_CHSCSD_MAXTXTIMESLOTS             (0x00000004) // @paramdefine
#define RIL_PARAM_CHSCSD_MAXTOTALTIMESLOTS          (0x00000008) // @paramdefine
#define RIL_PARAM_CHSCSD_CHANNELCODINGS             (0x00000010) // @paramdefine
#define RIL_PARAM_CHSCSD_AIRINTERFACEUSERRATES      (0x00000020) // @paramdefine
#define RIL_PARAM_CHSCSD_TOPRXTIMESLOTRANGE         (0x00000040) // @paramdefine
#define RIL_PARAM_CHSCSD_ALL                        (0x0000007f) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILCAPSPBENTRYLENGTH
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_CPBEL_MAXADDRESSLENGTH            (0x00000001) // @paramdefine
#define RIL_PARAM_CPBEL_MAXTEXTLENGTH               (0x00000002) // @paramdefine
#define RIL_PARAM_CPBEL_ALL                         (0x00000003) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILGPRSCONTEXT
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_GCONT_CONTEXTID                   (0x00000001) // @paramdefine
#define RIL_PARAM_GCONT_PROTOCOLTYPE                (0x00000002) // @paramdefine
#define RIL_PARAM_GCONT_ACCESSPOINTNAME             (0x00000004) // @paramdefine
#define RIL_PARAM_GCONT_ADDRESS                     (0x00000008) // @paramdefine
#define RIL_PARAM_GCONT_DATACOMPRESSION             (0x00000010) // @paramdefine
#define RIL_PARAM_GCONT_HEADERCOMPRESSION           (0x00000020) // @paramdefine
#define RIL_PARAM_GCONT_PARAMETERLENGTH             (0x00000040) // @paramdefine
#define RIL_PARAM_GCONT_PARAMETERS                  (0x00000080) // @paramdefine
#define RIL_PARAM_GCONT_ALL                         (0x000000ff) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILGPRSQOSPROFILE
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_GQOSP_CONTEXTID                   (0x00000001) // @paramdefine
#define RIL_PARAM_GQOSP_PRECEDENCECLASS             (0x00000002) // @paramdefine
#define RIL_PARAM_GQOSP_DELAYCLASS                  (0x00000004) // @paramdefine
#define RIL_PARAM_GQOSP_RELIABILITYCLASS            (0x00000008) // @paramdefine
#define RIL_PARAM_GQOSP_PEAKTHRUCLASS               (0x00000010) // @paramdefine
#define RIL_PARAM_GQOSP_MEANTHRUCLASS               (0x00000020) // @paramdefine
#define RIL_PARAM_GQOSP_ALL                         (0x0000003F) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILGPRSPACKETSUPPORT
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM__GPRSPACKET_PACKET                     (0x00000001) // @paramdefine
#define RIL_PARAM__GPRSPACKET_ACTIVECONTEXTS             (0x00000002) // @paramdefine

//
// Other constants
//

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants RIL_NOTIFY_NDIS | XFERSTATUSCHANGED
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_NDIS_XON TRUE
#define RIL_NDIS_XOFF FALSE
// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Address Type | Different phone number representations
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_ADDRTYPE_UNKNOWN                        (0x00000000)      // @constdefine Unknown type
#define RIL_ADDRTYPE_INTERNATIONAL                  (0x00000001)      // @constdefine International number
#define RIL_ADDRTYPE_NATIONAL                       (0x00000002)      // @constdefine National number
#define RIL_ADDRTYPE_NETWKSPECIFIC                  (0x00000003)      // @constdefine Network specific number
#define RIL_ADDRTYPE_SUBSCRIBER                     (0x00000004)      // @constdefine Subscriber number (protocol-specific)
#define RIL_ADDRTYPE_ALPHANUM                       (0x00000005)      // @constdefine Alphanumeric address
#define RIL_ADDRTYPE_ABBREV                         (0x00000006)      // @constdefine Abbreviated number
//additional CDMA ADDRTYPE definitions
//See IS-2000.5-A-1 page 509 table 2.7.1.3.2.4-2
#define RIL_ADDRTYPE_IP                             (0x00000007)      // @constdefine IP Address (RFC 791)
#define RIL_ADDRTYPE_EMAIL                          (0x00000008)      // @constdefine Internet Email addresss (RFC 822)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Numbering Plan | Different numbering shcemes
//
// @comm Used for <def RIL_ADDRTYPE_UNKNOWN>, <def RIL_ADDRTYPE_INTERNATIONAL>,
//       and <def RIL_ADDRTYPE_NATIONAL>
//
// -----------------------------------------------------------------------------
#define RIL_NUMPLAN_UNKNOWN                         (0x00000000)      // @constdefine Unknown numbering plan
#define RIL_NUMPLAN_TELEPHONE                       (0x00000001)      // @constdefine ISDN/telephone numbering plan (E.164/E.163)
#define RIL_NUMPLAN_DATA                            (0x00000002)      // @constdefine Data numbering plan (X.121)
#define RIL_NUMPLAN_TELEX                           (0x00000003)      // @constdefine Telex numbering plan
#define RIL_NUMPLAN_NATIONAL                        (0x00000004)      // @constdefine National numbering plan
#define RIL_NUMPLAN_PRIVATE                         (0x00000005)      // @constdefine Private numbering plan
#define RIL_NUMPLAN_ERMES                           (0x00000006)      // @constdefine ERMES numbering plan (ETSI DE/PS 3 01-3)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Subaddress Type | Different subaddress types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SUBADDRTYPE_NSAP                        (0x00000001)      // @constdefine NSAP subaddress (CCITT Recommendation X.213 or ISO 8348 AD2)
#define RIL_SUBADDRTYPE_USER                        (0x00000002)      // @constdefine User defined subaddress

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Data Rate | Defines different protocol dependant data rates
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SPEED_UNKNOWN                           (0x00000000)      // @constdefine Unknown speed
#define RIL_SPEED_AUTO                              (0x00000001)      // @constdefine Automatic selection of speed
#define RIL_SPEED_300_V21                           (0x00000002)      // @constdefine 300 bps (V.21)
#define RIL_SPEED_300_V110                          (0x00000003)      // @constdefine 300 bps (V.100)
#define RIL_SPEED_1200_V22                          (0x00000004)      // @constdefine 1200 bps (V.22)
#define RIL_SPEED_1200_75_V23                       (0x00000005)      // @constdefine 1200/75 bps (V.23)
#define RIL_SPEED_1200_V110                         (0x00000006)      // @constdefine 1200 bps (V.100)
#define RIL_SPEED_1200_V120                         (0x00000007)      // @constdefine 1200 bps (V.120)
#define RIL_SPEED_2400_V22BIS                       (0x00000008)      // @constdefine 2400 bps (V.22bis)
#define RIL_SPEED_2400_V26TER                       (0x00000009)      // @constdefine 2400 bps (V.26ter)
#define RIL_SPEED_2400_V110                         (0x0000000a)      // @constdefine 2400 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_2400_V120                         (0x0000000b)      // @constdefine 2400 bps (V.120)
#define RIL_SPEED_4800_V32                          (0x0000000c)      // @constdefine 4800 bps (V.32)
#define RIL_SPEED_4800_V110                         (0x0000000d)      // @constdefine 4800 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_4800_V120                         (0x0000000e)      // @constdefine 4800 bps (V.120)
#define RIL_SPEED_9600_V32                          (0x0000000f)      // @constdefine 9600 bps (V.32)
#define RIL_SPEED_9600_V34                          (0x00000010)      // @constdefine 9600 bps (V.34)
#define RIL_SPEED_9600_V110                         (0x00000011)      // @constdefine 9600 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_9600_V120                         (0x00000012)      // @constdefine 9600 bps (V.120)
#define RIL_SPEED_14400_V34                         (0x00000013)      // @constdefine 14400 bps (V.34)
#define RIL_SPEED_14400_V110                        (0x00000014)      // @constdefine 14400 bps (V.100 or X.31 flag stuffing)
#define RIL_SPEED_14400_V120                        (0x00000015)      // @constdefine 14400 bps (V.120)
#define RIL_SPEED_19200_V34                         (0x00000016)      // @constdefine 19200 bps (V.34)
#define RIL_SPEED_19200_V110                        (0x00000017)      // @constdefine 19200 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_19200_V120                        (0x00000018)      // @constdefine 19200 bps (V.120)
#define RIL_SPEED_28800_V34                         (0x00000019)      // @constdefine 28800 bps (V.34)
#define RIL_SPEED_28800_V110                        (0x0000001a)      // @constdefine 28800 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_28800_V120                        (0x0000001b)      // @constdefine 28800 bps (V.120)
#define RIL_SPEED_38400_V110                        (0x0000001c)      // @constdefine 38400 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_38400_V120                        (0x0000001d)      // @constdefine 38400 bps (V.120)
#define RIL_SPEED_48000_V110                        (0x0000001e)      // @constdefine 48000 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_48000_V120                        (0x0000001f)      // @constdefine 48000 bps (V.120)
#define RIL_SPEED_56000_V110                        (0x00000020)      // @constdefine 56000 bps (V.110 or X.31 flag stuffing)
#define RIL_SPEED_56000_V120                        (0x00000021)      // @constdefine 56000 bps (V.120)
#define RIL_SPEED_56000_TRANSP                      (0x00000022)      // @constdefine 56000 bps (bit transparent)
#define RIL_SPEED_64000_TRANSP                      (0x00000023)      // @constdefine 64000 bps (bit transparent)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Telephony Service | Telephony service types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SERVICE_UNKNOWN                         (0x00000000)      // @constdefine Unknown service
#define RIL_SERVICE_MODEM_ASYNC                     (0x00000001)      // @constdefine Asynchronous modem
#define RIL_SERVICE_MODEM_SYNC                      (0x00000002)      // @constdefine Synchronous modem
#define RIL_SERVICE_PADACCESS_ASYNC                 (0x00000003)      // @constdefine PAD Access (asynchronous)
#define RIL_SERVICE_PACKETACCESS_SYNC               (0x00000004)      // @constdefine Packet Access (synchronous)
#define RIL_SERVICE_VOICE                           (0x00000005)      // @constdefine Voice
#define RIL_SERVICE_FAX                             (0x00000006)      // @constdefine Fax

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants ITC | Information trasnfer capability types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_ITC_31KHZ                               (0x00000001)      // @constdefine 3.1 kHz
#define RIL_ITC_UDI                                 (0x00000002)      // @constdefine Unrestricted Digital Information

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Operator Name | Operator name formats
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_OPFORMAT_LONG                           (0x00000001)      // @constdefine Long alphanumeric name
#define RIL_OPFORMAT_SHORT                          (0x00000002)      // @constdefine Short alphanumeric name
#define RIL_OPFORMAT_NUM                            (0x00000003)      // @constdefine Numeric name

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Operator Status | Operator status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_OPSTATUS_UNKNOWN                        (0x00000000)      // @constdefine Unknown status
#define RIL_OPSTATUS_AVAILABLE                      (0x00000001)      // @constdefine Operator is available
#define RIL_OPSTATUS_CURRENT                        (0x00000002)      // @constdefine Operator is current
#define RIL_OPSTATUS_FORBIDDEN                      (0x00000003)      // @constdefine Operator is forbidden

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Operator Selection | Operator selection modes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_OPSELMODE_AUTOMATIC                     (0x00000001)      // @constdefine Automatic operator selection
#define RIL_OPSELMODE_MANUAL                        (0x00000002)      // @constdefine Manual operator selection
#define RIL_OPSELMODE_MANUALAUTOMATIC               (0x00000003)      // @constdefine Manual/automatic operator selection
                                                                      // (if manual selection fails, automatic selection mode is entered)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Operator Special | Special preferred operator index value
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PREFOPINDEX_FIRSTAVAILABLE              (0xffffffff)      // @constdefine Used to specify that a preferred operator is
                                                                      // to be stored at the first available index

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Information Class | Telephony information classes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_INFOCLASS_NONE                          (0x00000000)      // @constdefine None
#define RIL_INFOCLASS_VOICE                         (0x00000001)      // @constdefine Voice
#define RIL_INFOCLASS_DATA                          (0x00000002)      // @constdefine Data
#define RIL_INFOCLASS_FAX                           (0x00000004)      // @constdefine Fax
#define RIL_INFOCLASS_SMS                           (0x00000008)      // @constdefine SMS
#define RIL_INFOCLASS_DATACIRCUITSYNC               (0x00000010)      // @constdefine Data Circuit synchronous
#define RIL_INFOCLASS_DATACIRCUITASYNC              (0x00000020)      // @constdefine Data Circuit asynchronous
#define RIL_INFOCLASS_PACKETACCESS                  (0x00000040)      // @constdefine Dedicated Packet Access
#define RIL_INFOCLASS_PADACCESS                     (0x00000080)      // @constdefine Dedicated PAD Access
#define RIL_INFOCLASS_ALL                           (0x000000ff)      // @constdefine All information classes

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Supplemental Activation | Supplementary service status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SVCSTAT_UNKNOWN                         (0x00000000)      // @constdefine Unknown status
#define RIL_SVCSTAT_DISABLED                        (0x00000001)      // @constdefine Service is disabled
#define RIL_SVCSTAT_ENABLED                         (0x00000002)      // @constdefine Service is enabled
#define RIL_SVCSTAT_DEFAULT                         (0x00000003)      // @constdefine Default status

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Supplementary Service Provisioning | Supplementary service provisioning values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SVCPROV_UNKNOWN                         (0x00000000)      // @constdefine Unknown provisioning
#define RIL_SVCPROV_NOTPROVISIONED                  (0x00000001)      // @constdefine Service isn't provisioned
#define RIL_SVCPROV_PROVISIONED                     (0x00000002)      // @constdefine Service is provisioned
#define RIL_SVCPROV_TEMPMODERESTRICTED              (0x00000003)      // @constdefine Service temporary mode is restricted
#define RIL_SVCPROV_TEMPMODEALLOWED                 (0x00000004)      // @constdefine Service temporary mode is allowed

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CUG Special | Closed User Group special index value
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CUGINDEX_NONE                           (0xffffffff)      // @constdefine Used to identify the absence of CUG index

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CUG Info Level | Closed User Group information levels
//
// @comm This feature is not used and is untested.
//
// -----------------------------------------------------------------------------
#define RIL_CUGINFO_NONE                            (0x00000000)      // @constdefine TBD
#define RIL_CUGINFO_SUPPRESSOA                      (0x00000001)      // @constdefine TBD
#define RIL_CUGINFO_SUPRESSPREF                     (0x00000002)      // @constdefine TBD
#define RIL_CUGINFO_SUPPRESSOAANDPREF               (0x00000003)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Forwarding Reason | Forwarding reasons
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_FWDREASON_UNCONDITIONAL                 (0x00000001)      // @constdefine Always forward
#define RIL_FWDREASON_MOBILEBUSY                    (0x00000002)      // @constdefine Forward when device busy
#define RIL_FWDREASON_NOREPLY                       (0x00000003)      // @constdefine Forward when no answer
#define RIL_FWDREASON_UNREACHABLE                   (0x00000004)      // @constdefine Forward device out of service
#define RIL_FWDREASON_ALLFORWARDING                 (0x00000005)      // @constdefine TBD
#define RIL_FWDREASON_ALLCONDITIONAL                (0x00000006)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Type | Call types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CALLTYPE_UNKNOWN                        (0x00000000)      // @constdefine Unknown
#define RIL_CALLTYPE_VOICE                          (0x00000001)      // @constdefine Voice call
#define RIL_CALLTYPE_DATA                           (0x00000002)      // @constdefine Data call
#define RIL_CALLTYPE_FAX                            (0x00000003)      // @constdefine Fax call

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Dialing Option | Dialing options
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DIALOPT_NONE                            (0x00000000)      // @constdefine No options
#define RIL_DIALOPT_RESTRICTID                      (0x00000001)      // @constdefine Restrict CLI presentation
#define RIL_DIALOPT_PRESENTID                       (0x00000002)      // @constdefine Allow CLI presentation
#define RIL_DIALOPT_CLOSEDGROUP                     (0x00000004)      // @constdefine Closed User Group dialing
#define RIL_DIALOPT_ALL                             (0x00000007)      // @constdefine All options

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Option | Call options defaults
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DIALTONEWAIT_DEFAULT                    (0x00000000)      // @constdefine TBD
#define RIL_DIALTIMEOUT_DEFAULT                     (0x00000000)      // @constdefine TBD
#define RIL_COMMAPAUSE_DEFAULT                      (0x00000000)      // @constdefine TBD
#define RIL_DISCONNECTTIMEOUT_DEFAULT               (0x00000000)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants DTMF Duration | DTMF tone duration default
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_DTMFDURATION_DEFAULT                    (0x00000000)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Number of Calls to Track | Number of Calls to Track
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MAX_TRACKED_CALL_ID             10

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Direction | Call direction
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CALLDIR_INCOMING                        (0x00000001)      // @constdefine Incoming call
#define RIL_CALLDIR_OUTGOING                        (0x00000002)      // @constdefine Outgoing call

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Status | Call status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CALLSTAT_ACTIVE                         (0x00000001)      // @constdefine Active call
#define RIL_CALLSTAT_ONHOLD                         (0x00000002)      // @constdefine Call on hold
#define RIL_CALLSTAT_DIALING                        (0x00000003)      // @constdefine In the process of dialing
#define RIL_CALLSTAT_ALERTING                       (0x00000004)      // @constdefine In the process of ringing
#define RIL_CALLSTAT_INCOMING                       (0x00000005)      // @constdefine Incoming (unanswered) call
#define RIL_CALLSTAT_WAITING                        (0x00000006)      // @constdefine Incoming call waiting call

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CPI Status | CPI status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CPISTAT_UNKNOWN                         (0x00000000)      // @constdefine
#define RIL_CPISTAT_NEW_OUTGOING                    (0x00000001)      // @constdefine
#define RIL_CPISTAT_NEW_INCOMING                    (0x00000002)      // @constdefine
#define RIL_CPISTAT_CONNECTED                       (0x00000003)      // @constdefine
#define RIL_CPISTAT_DISCONNECTED                    (0x00000004)      // @constdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Intermediate Supplementary Service | Intermediate Supplementary Service Codes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_INTSSCODE_UNCONDITIONALCFACTIVE         (0x00000000)      // @constdefine Unconditional call forwarding is active
#define RIL_INTSSCODE_SOMECONDITIONALCFACTIVE       (0x00000001)      // @constdefine Some of the conditional call forwarding settings are active
#define RIL_INTSSCODE_CALLWASFORWARDED              (0x00000002)      // @constdefine Call has been forwarded
#define RIL_INTSSCODE_CALLISWAITING                 (0x00000003)      // @constdefine Call is waiting
#define RIL_INTSSCODE_CUGCALL                       (0x00000004)      // @constdefine This is a CUG call (also <index> present)
#define RIL_INTSSCODE_OUTGOINGCALLSBARRED           (0x00000005)      // @constdefine Outgoing calls are barred
#define RIL_INTSSCODE_INCOMINGCALLSBARRED           (0x00000006)      // @constdefine Incoming calls are barred
#define RIL_INTSSCODE_CLIRSUPPRESSREJECT            (0x00000007)      // @constdefine CLIR suppression rejected
#define RIL_INTSSCODE_CALLWASDEFLECTED              (0x00000008)      // @constdefine Call has been deflected

#define RIL_INTSSCODE_MAX              RIL_INTSSCODE_CALLWASDEFLECTED    // @constdefine Maximum valid value

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Unsolicited Supplementary Service | Unsolicited Supplementary Service Codes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_UNSSSCODE_FORWARDEDCALL                 (0x00000000)      // @constdefine This is a forwarded call (MT call setup)
#define RIL_UNSSSCODE_CUGCALL                       (0x00000001)      // @constdefine This is a CUG call (also <index> present) (MT call setup)
#define RIL_UNSSSCODE_CALLPUTONHOLD                 (0x00000002)      // @constdefine Call has been put on hold (during a voice call)
#define RIL_UNSSSCODE_CALLRETRIEVED                 (0x00000003)      // @constdefine Call has been retrieved (during a voice call)
#define RIL_UNSSSCODE_ENTEREDMULTIPARTY             (0x00000004)      // @constdefine Multiparty call entered (during a voice call)
#define RIL_UNSSSCODE_HELDCALLRELEASED              (0x00000005)      // @constdefine Call on hold has been released (this is not a SS notification) (during a voice call)
#define RIL_UNSSSCODE_FORWARDCHECKSS                (0x00000006)      // @constdefine Forward check SS message received (can be received whenever)
#define RIL_UNSSSCODE_ALERTINGEXPLICITCALLXFER      (0x00000007)      // @constdefine Call is being connected (alerting) with the remote party in alerting state in explicit call transfer operation (during a voice call)
#define RIL_UNSSSCODE_CONNECTEDEXPLICITCALLXFER     (0x00000008)      // @constdefine Call has been connected with the other remote party in explicit call transfer operation (also number and subaddress parameters may be present) (during a voice call or MT call setup)
#define RIL_UNSSSCODE_DEFLECTEDCALL                 (0x00000009)      // @constdefine This is a deflected call (MT call setup)
#define RIL_UNSSSCODE_ADDITIONALINCOMINGCF          (0x0000000a)      // @constdefine Additional incoming call forwarded

#define RIL_UNSSSCODE_MAX              RIL_UNSSSCODE_ADDITIONALINCOMINGCF    // @constdefine Maximum valid value

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Multiparty | Call multiparty status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CALL_SINGLEPARTY                        (0x00000000)      // @constdefine Not in a conference
#define RIL_CALL_MULTIPARTY                         (0x00000001)      // @constdefine Participating in a conference

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Management | Call management commands
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CALLCMD_RELEASEHELD                     (0x00000001)      // @constdefine Release all held calls or sets "busy" to a waiting call
#define RIL_CALLCMD_RELEASEACTIVE_ACCEPTHELD        (0x00000002)      // @constdefine Release all active calls and accepts a waiting/held call
#define RIL_CALLCMD_RELEASECALL                     (0x00000003)      // @constdefine Release the specified call
#define RIL_CALLCMD_HOLDACTIVE_ACCEPTHELD           (0x00000004)      // @constdefine Hold all active calls and accepts a waiting/held call
#define RIL_CALLCMD_HOLDALLBUTONE                   (0x00000005)      // @constdefine Hold all active calls, except for the specified call
#define RIL_CALLCMD_ADDHELDTOCONF                   (0x00000006)      // @constdefine Add all held calls to a conference
#define RIL_CALLCMD_ADDHELDTOCONF_DISCONNECT        (0x00000007)      // @constdefine Connect held calls to a conference and disconnect the user
#define RIL_CALLCMD_INVOKECCBS                      (0x00000008)      // @constdefine Invokes completion of calls to busy subscribers

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Line Status | Line status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_LINESTAT_UNKNOWN                        (0x00000000)      // @constdefine Unknown
#define RIL_LINESTAT_READY                          (0x00000001)      // @constdefine Line is ready
#define RIL_LINESTAT_UNAVAILABLE                    (0x00000002)      // @constdefine Line is unavailable
#define RIL_LINESTAT_RINGING                        (0x00000003)      // @constdefine Incoming call on the line
#define RIL_LINESTAT_CALLINPROGRESS                 (0x00000004)      // @constdefine Call in progress
#define RIL_LINESTAT_ASLEEP                         (0x00000005)      // @constdefine Line is asleep
#define RIL_LINESTAT_CONNECTING                     (0x00000006)      // @constdefine The phone is connecting to a call, but the call is not in progress yet

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Line Registration | Line registration status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_REGSTAT_UNKNOWN                         (0x00000000)      // @constdefine Registration unknown
#define RIL_REGSTAT_UNREGISTERED                    (0x00000001)      // @constdefine Unregistered
#define RIL_REGSTAT_HOME                            (0x00000002)      // @constdefine Registered on home network
#define RIL_REGSTAT_ATTEMPTING                      (0x00000003)      // @constdefine Attempting to register
#define RIL_REGSTAT_DENIED                          (0x00000004)      // @constdefine Registration denied
#define RIL_REGSTAT_ROAMING                         (0x00000005)      // @constdefine Registered on roaming network

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Audio Device | Audio devices
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_AUDIO_NONE                              (0x00000000)      // @constdefine No audio devices
#define RIL_AUDIO_HANDSET                           (0x00000001)      // @constdefine Handset
#define RIL_AUDIO_SPEAKERPHONE                      (0x00000002)      // @constdefine Speakerphone
#define RIL_AUDIO_HEADSET                           (0x00000003)      // @constdefine Headset
#define RIL_AUDIO_CARKIT                            (0x00000004)      // @constdefine Carkit

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants HSCSD Traffic Channel | HSCSD traffic channel codings
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_HSCSDCODING_UNKNOWN                     (0x00000000)      // @constdefine Unknown channel coding
#define RIL_HSCSDCODING_4800_FULLRATE               (0x00000001)      // @constdefine 4800 bits per second
#define RIL_HSCSDCODING_9600_FULLRATE               (0x00000002)      // @constdefine 9600 bits per second
#define RIL_HSCSDCODING_14400_FULLRATE              (0x00000004)      // @constdefine 14400 bits per second
#define RIL_HSCSDCODING_ALL                         (0x00000007)      // @constdefine All channel codings valid

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants HSCSD Air Interface | HSCSD air interface user rates
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_HSCSDAIURATE_UNKNOWN                    (0x00000000)      // @constdefine Air interface rate
#define RIL_HSCSDAIURATE_9600                       (0x00000001)      // @constdefine 9600 bits per second
#define RIL_HSCSDAIURATE_14400                      (0x00000002)      // @constdefine 14400 bits per second
#define RIL_HSCSDAIURATE_19200                      (0x00000003)      // @constdefine 19200 bits per second
#define RIL_HSCSDAIURATE_28800                      (0x00000004)      // @constdefine 28800 bits per second
#define RIL_HSCSDAIURATE_38400                      (0x00000005)      // @constdefine 38400 bits per second
#define RIL_HSCSDAIURATE_43200                      (0x00000006)      // @constdefine 43200 bits per second
#define RIL_HSCSDAIURATE_57600                      (0x00000007)      // @constdefine 57600 bits per second
#define RIL_HSCSDAIURATE_DEFAULT                    (0xffffffff)      // @constdefine A special value that indicates the radio stack
                                                                      //    should calculate the appropriate number of
                                                                      //    receive timeslots based on other paramaters

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants HSCSD Special | Special HSCSD receive timeslots value
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_HSCSDTIMESLOTS_DEFAULT                  (0x00000000)      // @constdefine Indicates that the radio stack should
                                                                      // calculate apropriate number of timeslots
#define RIL_HSCSDTIMESLOTSLIMIT_NONE                (0x00000000)      // @constdefine Indicates that number of receive numeslots will not
                                                                      //    be altered during the next non-transparent HSCSD call

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Data Compression | Data compression directions
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DATACOMPDIR_NONE                        (0x00000001)      // @constdefine No data compression
#define RIL_DATACOMPDIR_TRANSMIT                    (0x00000002)      // @constdefine Data compession when sending
#define RIL_DATACOMPDIR_RECEIVE                     (0x00000004)      // @constdefine Data compession when receiving
#define RIL_DATACOMPDIR_BOTH                        (0x00000008)      // @constdefine Bi-directional data compession

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Data Compression Negotiation | Data compression negotiation options
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DATACOMP_OPTIONAL                       (0x00000001)      // @constdefine Data compression optional
#define RIL_DATACOMP_REQUIRED                       (0x00000002)      // @constdefine Terminal will disconnect if no negotiation

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Error Correction | Error correction modes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_ECMODE_UNKNOWN                          (0x00000000)      // @constdefine TBD
#define RIL_ECMODE_DIRECT                           (0x00000001)      // @constdefine TBD
#define RIL_ECMODE_BUFFERED                         (0x00000002)      // @constdefine TBD
#define RIL_ECMODE_NODETECT                         (0x00000004)      // @constdefine TBD
#define RIL_ECMODE_DETECT                           (0x00000008)      // @constdefine TBD
#define RIL_ECMODE_ALTERNATIVE                      (0x00000010)      // @constdefine TBD
#define RIL_ECMODE_OPTIONAL_USEBUFFERED             (0x00000020)      // @constdefine TBD
#define RIL_ECMODE_OPTIONAL_USEDIRECT               (0x00000040)      // @constdefine TBD
#define RIL_ECMODE_REQUIRED                         (0x00000080)      // @constdefine TBD
#define RIL_ECMODE_REQUIRED_LAPMONLY                (0x00000100)      // @constdefine TBD
#define RIL_ECMODE_REQUIRED_ALTERNATIVEONLY         (0x00000200)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Bearer Service | Bearer service names
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_BSVCNAME_UNKNOWN                        (0x00000000)      // @constdefine TBD
#define RIL_BSVCNAME_DATACIRCUIT_ASYNC_UDI_MODEM    (0x00000001)      // @constdefine TBD
#define RIL_BSVCNAME_DATACIRCUIT_SYNC_UDI_MODEM     (0x00000002)      // @constdefine TBD
#define RIL_BSVCNAME_PADACCESS_ASYNC_UDI            (0x00000003)      // @constdefine TBD
#define RIL_BSVCNAME_PACKETACCESS_SYNC_UDI          (0x00000004)      // @constdefine TBD
#define RIL_BSVCNAME_DATACIRCUIT_ASYNC_RDI          (0x00000005)      // @constdefine TBD
#define RIL_BSVCNAME_DATACIRCUIT_SYNC_RDI           (0x00000006)      // @constdefine TBD
#define RIL_BSVCNAME_PADACCESS_ASYNC_RDI            (0x00000007)      // @constdefine TBD
#define RIL_BSVCNAME_PACKETACCESS_SYNC_RDI          (0x00000008)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Bearer Service CE | Bearer service connection elements
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_BSVCCE_UNKNOWN                          (0x00000000)      // @constdefine Bearer service unknown
#define RIL_BSVCCE_TRANSPARENT                      (0x00000001)      // @constdefine Link layer correction enabled
#define RIL_BSVCCE_NONTRANSPARENT                   (0x00000002)      // @constdefine No link layer correction present
#define RIL_BSVCCE_BOTH_TRANSPARENT                 (0x00000003)      // @constdefine Both available, transparent preferred
#define RIL_BSVCCE_BOTH_NONTRANSPARENT              (0x00000004)      // @constdefine Both available, non-transparent preferred

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Service | Messaging service types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGSVCTYPE_UNKNOWN                      (0x00000000)      // @constdefine Unknown
#define RIL_MSGSVCTYPE_PHASE2                       (0x00000001)      // @constdefine GSM 07.05 Phase 2 ver. 4.7.0 messaging service
#define RIL_MSGSVCTYPE_PHASE2PLUS                   (0x00000002)      // @constdefine GSM 07.05 Pahse 2+ messaging service

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Storage | Message storage locations
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGLOC_UNKNOWN                          (0x00000000)      // @constdefine Unknown
#define RIL_MSGLOC_BROADCAST                        (0x00000001)      // @constdefine Broadcast message storage location
#define RIL_MSGLOC_SIM                              (0x00000002)      // @constdefine SIM storage location
#define RIL_MSGLOC_STATUSREPORT                     (0x00000003)      // @constdefine Status report storage location

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants IS637 Teleservices | Message Teleservice types
//
// @comm TIA/EIA-41-D Supported Teleservices
//
// -----------------------------------------------------------------------------
#define RIL_MSGTELESERVICE_PAGING                   (0x00000001)    // @constdefine Wireless Paging Teleservice      CPT-95  //@ Only callback number
#define RIL_MSGTELESERVICE_MESSAGING                (0x00000002)    // @constdefine Wireless Messaging Teleservice   CMT-95  //@ Text Message
#define RIL_MSGTELESERVICE_VOICEMAIL                (0x00000003)    // @constdefine Voice Mail Notification          VMN-95  //@ Voice Mail
#define RIL_MSGTELESERVICE_WAP                      (0x00000004)    // @constdefine Wireless Application Protocol    WAP     //@ To be investigated (Test message??)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Privacy Class | Message Privacy types
//
// @comm IS-637 Message Privacy Classes
//
// -----------------------------------------------------------------------------
#define RIL_MSGPRIVACYCLASS_NOTRESTRICTED           (0x00000001) // @constdefine Not restricted (Level 0)
#define RIL_MSGPRIVACYCLASS_RESTRICTED              (0x00000002) // @constdefine Restricted (Level 1)
#define RIL_MSGPRIVACYCLASS_CONFIDENTIAL            (0x00000003) // @constdefine Confidential (Level 2)
#define RIL_MSGPRIVACYCLASS_SECRET                  (0x00000004) // @constdefine Secret (Level 3)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Priority Class | Message Priority types
//
// @comm IS-637 Message Priority Classes
//
// -----------------------------------------------------------------------------
#define RIL_MSGPRIORITY_NORMAL                      (0x00000001)    // @constdefine Message Urgency Normal
#define RIL_MSGPRIORITY_HIGH                        (0x00000002)    // @constdefine Message Urgency Interactive (S N/A)
#define RIL_MSGPRIORITY_URGENT                      (0x00000003)    // @constdefine Message Urgency Urgent
#define RIL_MSGPRIORITY_EMERGENCY                   (0x00000004)    // @constdefine Message Urgency Emergency (S N/A)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Status Message Class | Message Statustypes
//
// @comm IS-637 Message Priority Classes
//
// -----------------------------------------------------------------------------
#define RIL_MSGSTATUSTYPE_BEARERACK                 (0x00000001)    // @constdefine The Acknowledgement Message is a Bearer Ack
#define RIL_MSGSTATUSTYPE_DELIVERYACK               (0x00000002)    // @constdefine The Acknowledgement Message is a Delivery Ack
#define RIL_MSGSTATUSTYPE_USERACK                   (0x00000003)    // @constdefine The Acknowledgement Message is a User Ack

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Display Modes | Message Display Modes
//
// @comm Message Display Modes - Determines if the message is shown immediately or in the inbox (Ask Carrier if this feature is implemented)
//
// -----------------------------------------------------------------------------
#define RIL_MSGDISPLAYMODE_IMMEDIATE                (0x00000001)    // @constdefine The message must be show immediately.
//In the UI, Mobile Default and User Default should be treated as the same.
#define RIL_MSGDISPLAYMODE_MOBILEDEFAULT            (0x00000002)    // @constdefine The message is to be displayed depending on a predefined mobile setting.
#define RIL_MSGDISPLAYMODE_USERDEFAULT              (0x00000003)    // @constdefine The message is to be displayed depending on the user's mode.

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Encoding | Message Encoding types
//
// @comm Message Encoding - Determines the format of the incoming message body
//
// -----------------------------------------------------------------------------
//Analog Only -#define RIL_MSGCODING_IS91EPP                       (0x00000001)    // @constdefine IS-91 Character Format
#define RIL_MSGCODING_7BITASCII                     (0x00000002)    // @constdefine This the the verizon default
#define RIL_MSGCODING_UNICODE                       (0x00000003)    // @constdefine Unicode (double byte) format
#define RIL_MSGCODING_7BITGSM                       (0x00000004)    // @constdefine 7-bit GSM Alphabet
#define RIL_MSGCODING_8BITGSM                       (0x00000005)    // @constdefine 8-bit GSM Alphabet

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message DCS | Message data coding scheme types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DCSTYPE_GENERAL                         (0x00000001)      // @constdefine TBD
#define RIL_DCSTYPE_MSGWAIT                         (0x00000002)      // @constdefine TBD
#define RIL_DCSTYPE_MSGCLASS                        (0x00000003)      // @constdefine TBD
#define RIL_DCSTYPE_LANGUAGE                        (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message DCS Flags | Message data coding scheme flags
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DCSFLAG_NONE                            (0x00000000)      // @constdefine TBD
#define RIL_DCSFLAG_COMPRESSED                      (0x00000001)      // @constdefine TBD
#define RIL_DCSFLAG_INDICATIONACTIVE                (0x00000002)      // @constdefine TBD
#define RIL_DCSFLAG_DISCARD                         (0x00000004)      // @constdefine Only for RIL_DCSTYPE_MSGWAIT
#define RIL_DCSFLAG_ALL                             (0x00000007)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message DCS Classes | Message data coding scheme message classes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DCSMSGCLASS_0                           (0x00000001)      // @constdefine TBD
#define RIL_DCSMSGCLASS_1                           (0x00000002)      // @constdefine TBD
#define RIL_DCSMSGCLASS_2                           (0x00000003)      // @constdefine TBD
#define RIL_DCSMSGCLASS_3                           (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message DCS Alphabets | Message data coding scheme alphabets
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DCSALPHABET_DEFAULT                     (0x00000001)      // @constdefine TBD
#define RIL_DCSALPHABET_8BIT                        (0x00000002)      // @constdefine TBD
#define RIL_DCSALPHABET_UCS2                        (0x00000003)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message DCS Indication | Message data coding scheme indication types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DCSINDICATION_VOICEMAIL                 (0x00000001)      // @constdefine Voicemail indication
#define RIL_DCSINDICATION_FAX                       (0x00000002)      // @constdefine Fax indication
#define RIL_DCSINDICATION_EMAIL                     (0x00000003)      // @constdefine E-Mail indication
#define RIL_DCSINDICATION_OTHER                     (0x00000004)      // @constdefine Other indication

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message DCS Broadcast| Message broadcast data coding scheme languages
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_DCSLANG_UNKNOWN                         (0x00000001)      // @constdefine TBD
#define RIL_DCSLANG_GERMAN                          (0x00000002)      // @constdefine TBD
#define RIL_DCSLANG_ENGLISH                         (0x00000004)      // @constdefine TBD
#define RIL_DCSLANG_ITALIAN                         (0x00000008)      // @constdefine TBD
#define RIL_DCSLANG_FRENCH                          (0x00000010)      // @constdefine TBD
#define RIL_DCSLANG_SPANISH                         (0x00000020)      // @constdefine TBD
#define RIL_DCSLANG_DUTCH                           (0x00000040)      // @constdefine TBD
#define RIL_DCSLANG_SWEDISH                         (0x00000080)      // @constdefine TBD
#define RIL_DCSLANG_DANISH                          (0x00000100)      // @constdefine TBD
#define RIL_DCSLANG_PORTUGUESE                      (0x00000200)      // @constdefine TBD
#define RIL_DCSLANG_FINNISH                         (0x00000400)      // @constdefine TBD
#define RIL_DCSLANG_NORWEGIAN                       (0x00000800)      // @constdefine TBD
#define RIL_DCSLANG_GREEK                           (0x00001000)      // @constdefine TBD
#define RIL_DCSLANG_TURKISH                         (0x00002000)      // @constdefine TBD
#define RIL_DCSLANG_HUNGARIAN                       (0x00004000)      // @constdefine TBD
#define RIL_DCSLANG_POLISH                          (0x00008000)      // @constdefine TBD
#define RIL_DCSLANG_CZECH                           (0x00010000)      // @constdefine TBD
#define RIL_DCSLANG_ALL                             (0x0001ffff)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Class | Message classes
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGCLASS_NONE                           (0x00000000)      // @constdefine TBD
#define RIL_MSGCLASS_INCOMING                       (0x00010000)      // @constdefine TBD
#define RIL_MSGCLASS_OUTGOING                       (0x00020000)      // @constdefine TBD
#define RIL_MSGCLASS_BROADCAST                      (0x00040000)      // @constdefine TBD
#define RIL_MSGCLASS_ALL                            (0x00070000)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Type | Message types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGTYPE_IN_DELIVER                      (0x00000001 | RIL_MSGCLASS_INCOMING)      // @constdefine Incoming message
#define RIL_MSGTYPE_IN_STATUS                       (0x00000002 | RIL_MSGCLASS_INCOMING)      // @constdefine Incoming status message
#define RIL_MSGTYPE_IN_IS637DELIVER                 (0x00000004 | RIL_MSGCLASS_INCOMING)      // @constdefine Incoming message
#define RIL_MSGTYPE_IN_IS637STATUS                  (0x00000008 | RIL_MSGCLASS_INCOMING)      // @constdefine Incoming status message (Both Delivery ACKs and User Acks)
#define RIL_MSGTYPE_OUT_SUBMIT                      (0x00000001 | RIL_MSGCLASS_OUTGOING)      // @constdefine Outgoing message
#define RIL_MSGTYPE_OUT_COMMAND                     (0x00000002 | RIL_MSGCLASS_OUTGOING)      // @constdefine Outgoing command message
#define RIL_MSGTYPE_OUT_RAW                         (0x00000004 | RIL_MSGCLASS_OUTGOING)      // @constdefine ???
#define RIL_MSGTYPE_OUT_IS637SUBMIT                 (0x00000008 | RIL_MSGCLASS_OUTGOING)      // @constdefine Outgoing message
#define RIL_MSGTYPE_OUT_IS637STATUS                 (0x00000010 | RIL_MSGCLASS_OUTGOING)      // @constdefine IS-637 User ACK in either direction
#define RIL_MSGTYPE_BC_GENERAL                      (0x00000001 | RIL_MSGCLASS_BROADCAST)     // @constdefine Broadcast message (incoming only)

// Macro to extract message class from message type
#define MSGCLASS_FROM_MSGTYPE(type)                 ((type) & 0xffff0000)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Flag | Message flags
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGFLAG_NONE                            (0x00000000)      // @constdefine None
#define RIL_MSGFLAG_MORETOSEND                      (0x00000001)      // @constdefine More messages to send (valid for <def RIL_MSGTYPE_IN_DELIVER> and <def RIL_MSGTYPE_IN_STATUS>)
#define RIL_MSGFLAG_REPLYPATH                       (0x00000002)      // @constdefine Message contains a reply path  (valid for <def RIL_MSGTYPE_IN_DELIVER> and <def RIL_MSGTYPE_OUT_SUBMIT>)
#define RIL_MSGFLAG_HEADER                          (0x00000004)      // @constdefine TBD (valid for <def RIL_MSGTYPE_IN_DELIVER>, <def RIL_MSGTYPE_OUT_SUBMIT>,
                                                                      //    <def RIL_MSGTYPE_IN_STATUS>, and <def RIL_MSGTYPE_OUT_COMMAND>)
#define RIL_MSGFLAG_REJECTDUPS                      (0x00000008)      // @constdefine TBD (valid for <def RIL_MSGTYPE_OUT_SUBMIT> only)
#define RIL_MSGFLAG_STATUSREPORTRETURNED            (0x00000010)      // @constdefine (valid for <def RIL_MSGTYPE_IN_DELIVER> only)
#define RIL_MSGFLAG_STATUSREPORTREQUESTED           (0x00000020)      // @constdefine (valid for <def RIL_MSGTYPE_OUT_SUBMIT> and <def RIL_MSGTYPE_OUT_COMMAND>)
#define RIL_MSGFLAG_CAUSEDBYCOMMAND                 (0x00000040)      // @constdefine (valid for <def RIL_MSGTYPE_IN_STATUS> only)
#define RIL_MSGFLAG_ALL                             (0x0000007f)      // @constdefine All flags are on

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Protocol | Message protocol IDs
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGPROTOCOL_UNKNOWN                     (0x00000000)      // @constdefine TBD
#define RIL_MSGPROTOCOL_SMETOSME                    (0x00000001)      // @constdefine TBD
#define RIL_MSGPROTOCOL_IMPLICIT                    (0x00000002)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELEX                       (0x00000003)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELEFAX_GROUP3              (0x00000004)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELEFAX_GROUP4              (0x00000005)      // @constdefine TBD
#define RIL_MSGPROTOCOL_VOICEPHONE                  (0x00000006)      // @constdefine TBD
#define RIL_MSGPROTOCOL_ERMES                       (0x00000007)      // @constdefine TBD
#define RIL_MSGPROTOCOL_PAGING                      (0x00000008)      // @constdefine TBD
#define RIL_MSGPROTOCOL_VIDEOTEX                    (0x00000009)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELETEX                     (0x0000000a)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELETEX_PSPDN               (0x0000000b)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELETEX_CSPDN               (0x0000000c)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELETEX_PSTN                (0x0000000d)      // @constdefine TBD
#define RIL_MSGPROTOCOL_TELETEX_ISDN                (0x0000000e)      // @constdefine TBD
#define RIL_MSGPROTOCOL_UCI                         (0x0000000f)      // @constdefine TBD
#define RIL_MSGPROTOCOL_MSGHANDLING                 (0x00000010)      // @constdefine TBD
#define RIL_MSGPROTOCOL_X400                        (0x00000011)      // @constdefine TBD
#define RIL_MSGPROTOCOL_EMAIL                       (0x00000012)      // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC1             (0x00000013)     // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC2             (0x00000014)     // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC3             (0x00000015)     // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC4             (0x00000016)     // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC5             (0x00000017)     // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC6             (0x00000018)     // @constdefine TBD
#define RIL_MSGPROTOCOL_SCSPECIFIC7             (0x00000019)     // @constdefine TBD
#define RIL_MSGPROTOCOL_GSMSTATION                  (0x0000001a)      // @constdefine TBD
#define RIL_MSGPROTOCOL_SM_TYPE0                    (0x0000001b)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE1                   (0x0000001c)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE2                   (0x0000001d)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE3                   (0x0000001e)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE4                   (0x0000001f)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE5                   (0x00000020)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE6                   (0x00000021)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RSM_TYPE7                   (0x00000022)      // @constdefine TBD
#define RIL_MSGPROTOCOL_RETURNCALL                  (0x00000023)      // @constdefine TBD
#define RIL_MSGPROTOCOL_ME_DOWNLOAD                 (0x00000024)      // @constdefine TBD
#define RIL_MSGPROTOCOL_DEPERSONALIZATION           (0x00000025)      // @constdefine TBD
#define RIL_MSGPROTOCOL_SIM_DOWNLOAD                (0x00000026)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Delivery | Message delivery status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGDLVSTATUS_RECEIVEDBYSME              (0x00000001)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_FORWARDEDTOSME             (0x00000002)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_REPLACEDBYSC               (0x00000003)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_CONGESTION_TRYING          (0x00000004)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SMEBUSY_TRYING             (0x00000005)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SMENOTRESPONDING_TRYING    (0x00000006)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SVCREJECTED_TRYING         (0x00000007)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_QUALITYUNAVAIL_TRYING      (0x00000008)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SMEERROR_TRYING            (0x00000009)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_CONGESTION                 (0x0000000a)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SMEBUSY                    (0x0000000b)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SMENOTRESPONDING           (0x0000000c)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SVCREJECTED                (0x0000000d)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_QUALITYUNAVAIL_TEMP        (0x0000000e)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SMEERROR                   (0x0000000f)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_REMOTEPROCERROR            (0x00000010)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_INCOMPATIBLEDEST           (0x00000011)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_CONNECTIONREJECTED         (0x00000012)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_NOTOBTAINABLE              (0x00000013)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_NOINTERNETWORKING          (0x00000014)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_VPEXPIRED                  (0x00000015)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_DELETEDBYORIGSME           (0x00000016)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_DELETEDBYSC                (0x00000017)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_NOLONGEREXISTS             (0x00000018)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_QUALITYUNAVAIL             (0x00000019)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_RESERVED_COMPLETED         (0x0000001a)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_RESERVED_TRYING            (0x0000001b)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_RESERVED_ERROR             (0x0000001c)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_RESERVED_TMPERROR          (0x0000001d)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SCSPECIFIC_COMPLETED       (0x0000001e)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SCSPECIFIC_TRYING          (0x0000001f)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SCSPECIFIC_ERROR           (0x00000020)      // @constdefine TBD
#define RIL_MSGDLVSTATUS_SCSPECIFIC_TMPERROR        (0x00000021)      // @constdefine TBD



// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Validity | Message validity period formats
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGVP_NONE                              (0x00000000)      // @constdefine TBD
#define RIL_MSGVP_RELATIVE                          (0x00000001)      // @constdefine TBD
#define RIL_MSGVP_ENHANCED                          (0x00000002)      // @constdefine TBD
#define RIL_MSGVP_ABSOLUTE                          (0x00000003)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Command | Message command types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGCMDTYPE_STATUSREQ                    (0x00000001)      // @constdefine TBD
#define RIL_MSGCMDTYPE_CANCELSTATUSREQ              (0x00000002)      // @constdefine TBD
#define RIL_MSGCMDTYPE_DELETEMESSAGE                (0x00000003)      // @constdefine TBD
#define RIL_MSGCMDTYPE_ENABLESTATUSREQ              (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Geographic | Message geographic scopes
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GEOSCOPE_CELL_IMMEDIATE                 (0x00000001)      // @constdefine TBD
#define RIL_GEOSCOPE_CELL                           (0x00000002)      // @constdefine TBD
#define RIL_GEOSCOPE_PLMN                           (0x00000003)      // @constdefine TBD
#define RIL_GEOSCOPE_LOCATIONAREA                   (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Status | Message status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_MSGSTATUS_UNKNOWN                       (0x00000000)      // @constdefine TBD
#define RIL_MSGSTATUS_RECUNREAD                     (0x00000001)      // @constdefine TBD
#define RIL_MSGSTATUS_RECREAD                       (0x00000002)      // @constdefine TBD
#define RIL_MSGSTATUS_STOUNSENT                     (0x00000003)      // @constdefine TBD
#define RIL_MSGSTATUS_STOSENT                       (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Message Send | Send message options
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SENDOPT_NONE                            (0x00000000)      // @constdefine TBD
#define RIL_SENDOPT_PERSISTLINK                     (0x00000001)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Phone Locked | Phone locked states
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_LOCKEDSTATE_UNKNOWN                     (0x00000000)      // @constdefine Locking state unknown
#define RIL_LOCKEDSTATE_READY                       (0x00000001)      // @constdefine ME not locked
#define RIL_LOCKEDSTATE_SIM_PIN                     (0x00000002)      // @constdefine ME awaiting PIN
#define RIL_LOCKEDSTATE_SIM_PUK                     (0x00000003)      // @constdefine ME awaiting PUK
#define RIL_LOCKEDSTATE_PH_SIM_PIN                  (0x00000004)      // @constdefine ME awaiting phone-to-sim password
#define RIL_LOCKEDSTATE_PH_FSIM_PIN                 (0x00000005)      // @constdefine ME awaiting phone-to-first-sim password
#define RIL_LOCKEDSTATE_PH_FSIM_PUK                 (0x00000006)      // @constdefine ME awaiting phone-to-first-sim PUK
#define RIL_LOCKEDSTATE_SIM_PIN2                    (0x00000007)      // @constdefine ME awaiting PIN2/CHV2
#define RIL_LOCKEDSTATE_SIM_PUK2                    (0x00000008)      // @constdefine ME awaiting PUK2
#define RIL_LOCKEDSTATE_PH_NET_PIN                  (0x00000009)      // @constdefine ME awaiting network personilzation PIN
#define RIL_LOCKEDSTATE_PH_NET_PUK                  (0x0000000a)      // @constdefine ME awaiting network personilzation PUK
#define RIL_LOCKEDSTATE_PH_NETSUB_PIN               (0x0000000b)      // @constdefine ME awaiting network subset personilzation PIN
#define RIL_LOCKEDSTATE_PH_NETSUB_PUK               (0x0000000c)      // @constdefine ME awaiting network subset personilzation PUK
#define RIL_LOCKEDSTATE_PH_SP_PIN                   (0x0000000d)      // @constdefine ME awaiting service provider PIN
#define RIL_LOCKEDSTATE_PH_SP_PUK                   (0x0000000e)      // @constdefine ME awaiting service provider PUK
#define RIL_LOCKEDSTATE_PH_CORP_PIN                 (0x0000000f)      // @constdefine ME awaiting corporate personilzation PIN
#define RIL_LOCKEDSTATE_PH_CORP_PUK                 (0x00000010)      // @constdefine ME awaiting corporate personilzation PUK

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Lock Facility | Facilities for phone locking
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_LOCKFACILITY_CNTRL                      (0x00000001)      // @constdefine Lock control curface
#define RIL_LOCKFACILITY_PH_SIM                     (0x00000002)      // @constdefine Lock phone to SIM card
#define RIL_LOCKFACILITY_PH_FSIM                    (0x00000003)      // @constdefine Lock phone to first SIM card
#define RIL_LOCKFACILITY_SIM                        (0x00000004)      // @constdefine Lock SIM card
#define RIL_LOCKFACILITY_SIM_PIN2                   (0x00000005)      // @constdefine SIM PIN2 (only for RIL_ChangeLockingPassword())
#define RIL_LOCKFACILITY_SIM_FIXEDIALING            (0x00000006)      // @constdefine SIM fixed dialing memory
#define RIL_LOCKFACILITY_NETWORKPERS                (0x00000007)      // @constdefine Network personalization
#define RIL_LOCKFACILITY_NETWORKSUBPERS             (0x00000008)      // @constdefine Network subset personalization
#define RIL_LOCKFACILITY_SERVICEPROVPERS            (0x00000009)      // @constdefine Service provider personalization
#define RIL_LOCKFACILITY_CORPPERS                   (0x0000000a)      // @constdefine Corporate personalization

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Lock Status | Locking status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_LOCKINGSTATUS_DISABLED                  (0x00000001)      // @constdefine Disable
#define RIL_LOCKINGSTATUS_ENABLED                   (0x00000002)      // @constdefine Enabled

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Barr Facility | Types of call barring
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_BARRTYPE_ALLOUTGOING                    (0x00000001)      // @constdefine Barr all outgoing calls
#define RIL_BARRTYPE_OUTGOINGINT                    (0x00000002)      // @constdefine Barr outgoing international calls
#define RIL_BARRTYPE_OUTGOINGINTEXTOHOME            (0x00000003)      // @constdefine Barr outgoing international calls except to home country
#define RIL_BARRTYPE_ALLINCOMING                    (0x00000004)      // @constdefine Barr all incoming calls
#define RIL_BARRTYPE_INCOMINGROAMING                (0x00000005)      // @constdefine Barr incoming calls when roaming outside of home country
#define RIL_BARRTYPE_INCOMINGNOTINSIM               (0x00000006)      // @constdefine Barr incoming calls from numbers not stored to SIM memory
#define RIL_BARRTYPE_ALLBARRING                     (0x00000007)      // @constdefine All barring services
#define RIL_BARRTYPE_ALLOUTGOINGBARRING             (0x00000008)      // @constdefine All outgoing barring services
#define RIL_BARRTYPE_ALLINCOMINGBARRING             (0x00000009)      // @constdefine All incoming barring services

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Call Barr Status | Status values for call barring
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_BARRINGSTATUS_DISABLED                  (0x00000001)      // @constdefine Disable
#define RIL_BARRINGSTATUS_ENABLED                   (0x00000002)      // @constdefine Disable

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Equipment State | Equipment states
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_EQSTATE_UNKNOWN                         (0x00000000)      // @constdefine Unknown
#define RIL_EQSTATE_MINIMUM                         (0x00000001)      // @constdefine Minimum power state
#define RIL_EQSTATE_FULL                            (0x00000002)      // @constdefine Full functionality
#define RIL_EQSTATE_DISABLETX                       (0x00000003)      // @constdefine Transmitter disabled
#define RIL_EQSTATE_DISABLERX                       (0x00000004)      // @constdefine Receiver disabled
#define RIL_EQSTATE_DISABLETXANDRX                  (0x00000005)      // @constdefine Transmitter & receiver disabled

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Radio Presence States | Radio Presence States
//
// @comm These states are determined by whether the driver is loaded or not
//
// -----------------------------------------------------------------------------
#define RIL_RADIOPRESENCE_NOTPRESENT                (0x00000000)      // @constdefine There is not radio module present in the device
#define RIL_RADIOPRESENCE_PRESENT                   (0x00000001)      // @constdefine There is a radio module present that RIL can use

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Driver defined Radio ON vs OFF State | Radio ON/OFF states
//
// @comm These values normally depend on the Equiptment state
//
// -----------------------------------------------------------------------------
#define RIL_RADIOSUPPORT_UNKNOWN                    (0x00000000)      // @constdefine The Radio Functionality is in an intermediate state
#define RIL_RADIOSUPPORT_OFF                        (0x00000001)      // @constdefine The Radio Functionality is OFF (DOES NOT Neccessarily mean safe for flight)
#define RIL_RADIOSUPPORT_ON                         (0x00000002)      // @constdefine The Radio Functionality is ON

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Various components of the Radio are ready for external usage
//
// @comm This will be a mask of the below values
//
// -----------------------------------------------------------------------------
#define RIL_READYSTATE_NONE                         (0x00000000)      // @constdefine Nothing is ready yet
#define RIL_READYSTATE_INITIALIZED                  (0x00000001)      // @constdefine The Radio has been initialized (but may not be ready)
#define RIL_READYSTATE_SIM                          (0x00000002)      // @constdefine The Radio is ready for SIM Access
#define RIL_READYSTATE_SMS                          (0x00000004)      // @constdefine The Radio is ready for SMS messages
#define RIL_READYSTATE_UNLOCKED                     (0x00000008)      // @constdefine The SIM is unlocked
#define RIL_READYSTATE_SIM_PB                       (0x00000010)      // @constdefine The SIM PB has been fully copied to volatile memory and is ready for access

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Various SIM card states.
//
// @comm NONE
//
// -----------------------------------------------------------------------------
#define RIL_SIMSTATUSCHANGED_NONE                   (0x00000000)      // @constdefine No status yet
#define RIL_SIMSTATUSCHANGED_FULL                   (0x00000001)      // @constdefine SIM card memory is full
#define RIL_SIMSTATUSCHANGED_NO_SIM                 (0x00000002)      // @constdefine No SIM card available
#define RIL_SIMSTATUSCHANGED_INVALID                (0x00000004)      // @constdefine SIM card is invalid
#define RIL_SIMSTATUSCHANGED_BLOCKED                (0x00000008)      // @constdefine SIM card is blocked

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Phonebook Storage | Phonebook storage locations
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PBLOC_UNKNOWN                           (0x00000000)      // @constdefine Unknown
#define RIL_PBLOC_SIMEMERGENCY                      (0x00000001)      // @constdefine Emergency numbers
#define RIL_PBLOC_SIMFIXDIALING                     (0x00000002)      // @constdefine Fixed dialing
#define RIL_PBLOC_SIMLASTDIALING                    (0x00000003)      // @constdefine Recent calls list
#define RIL_PBLOC_OWNNUMBERS                        (0x00000004)      // @constdefine TBD
#define RIL_PBLOC_SIMPHONEBOOK                      (0x00000005)      // @constdefine SIM phonebook

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Special Phonebook | Special phonebook index value
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PBINDEX_FIRSTAVAILABLE                  (0xffffffff)      // @constdefine User first available entry

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants SIM Command | SIM commands
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SIMCMD_READBINARY                       (0x00000001)      // @constdefine Read a binary
#define RIL_SIMCMD_READRECORD                       (0x00000002)      // @constdefine Read contents of a record
#define RIL_SIMCMD_GETRESPONSE                      (0x00000003)      // @constdefine Required to get output data for some commands
#define RIL_SIMCMD_UPDATEBINARY                     (0x00000004)      // @constdefine Update a transparent file
#define RIL_SIMCMD_UPDATERECORD                     (0x00000005)      // @constdefine Update a linear fixed or cyclic file
#define RIL_SIMCMD_STATUS                           (0x00000006)      // @constdefine Get status on a file

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants SIM Record | Different SIM file types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SIMRECORDTYPE_UNKNOWN          (0x00000000)         // @constdefine An unknown file type
#define RIL_SIMRECORDTYPE_TRANSPARENT      (0x00000001)         // @constdefine A single veriable lengthed record
#define RIL_SIMRECORDTYPE_CYCLIC           (0x00000002)         // @constdefine A cyclic set of records, each of the same length
#define RIL_SIMRECORDTYPE_LINEAR           (0x00000003)         // @constdefine A linear set of records, each of the same length
#define RIL_SIMRECORDTYPE_MASTER           (0x00000004)         // @constdefine Every SIM has a single master record, effectively the head node
#define RIL_SIMRECORDTYPE_DEDICATED        (0x00000005)         // @constdefine Effectively a "directory" file which is a parent of other records

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants SIM Toolkit Terminate | SIM Toolkit session termination causes
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_SIMTKITTERMCAUSE_USERSTOPPEDREDIAL      (0x00000001)      // @constdefine User stopped redial attempts
#define RIL_SIMTKITTERMCAUSE_ENDOFREDIAL            (0x00000002)      // @constdefine End of redial period
#define RIL_SIMTKITTERMCAUSE_USERENDEDSESSION       (0x00000003)      // @constdefine Session terminated by user


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILSIMTOOLKITNOTIFYCAPS
//
// @constants Unavailable | Detailed reason for support of toolkit functions
//
// @comm Values that variables information variables in RILSIMTOOLKITNOTIFYCAPS can take on
//
// -----------------------------------------------------------------------------
//
#define RIL_SIMTKN_MEIMPLEMENTS                     (0x00000001)     // @constdefine The ME must implement this notification
#define RIL_SIMTKN_RADIOIMPLEMENTS_NONOTIFICATION   (0x00000002)     // @constdefine The radio will implement and not give a notification to the ME
#define RIL_SIMTKN_RADIOIMPLEMENTS_NOTIFICATION     (0x00000003)     // @constdefine The radio will implement and give a notification to the ME that it was done
#define RIL_SIMTKN_RADIOIMPLEMENTS_REQUESTMEINPUT   (0x00000004)     // @constdefine The radio will implement, but requests information from the ME first
#define RIL_SIMTKN_NOSUPPORT                        (0xFFFFFFFF)     // @constdefine RIL knows of this type of command but does not support.

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Signal Strength | Special signal strength value
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_SIGNALSTRENGTH_UNKNOWN                  (0xffffffff)      // @constdefine Unknown signal strength

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Bit Error Rate | Special bit error rate value
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_BITERRORRATE_UNKNOWN                    (0xffffffff)      // @constdefine Unknown signal strength

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Remote Party | Remote party information validity types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_REMOTEPARTYINFO_VALID                   (0x00000001)      // @constdefine Information valid
#define RIL_REMOTEPARTYINFO_WITHHELD                (0x00000002)      // @constdefine Information withheld by other user
#define RIL_REMOTEPARTYINFO_UNAVAILABLE             (0x00000003)      // @constdefine Network unable to send info

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Disconnect Initiation | Disconnect initiation values
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_DISCINIT_NULL                           (0x00000000)      // @constdefine Nothing
#define RIL_DISCINIT_LOCAL                          (0x00000001)      // @constdefine Local party initiated
#define RIL_DISCINIT_REMOTE                         (0x00000002)      // @constdefine Remote party initiated
#define RIL_DISCINIT_NETWORKERROR                   (0x00000003)      // @constdefine The call was disconnected due to a network error condition
#define RIL_DISCINIT_BUSY                           (0x00000004)      // @constdefine Busy

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Subaddress Type | Supplementary service data status values
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SUPSVCDATASTATUS_NOINFOREQUIRED         (0x00000001)      // @constdefine TBD
#define RIL_SUPSVCDATASTATUS_FURTHERINFOREQUIRED    (0x00000002)      // @constdefine TBD
#define RIL_SUPSVCDATASTATUS_TERMINATED             (0x00000003)      // @constdefine TBD
#define RIL_SUPSVCDATASTATUS_OTHERCLIENTRESPONDED   (0x00000004)      // @constdefine TBD
#define RIL_SUPSVCDATASTATUS_UNSUPPORTED            (0x00000005)      // @constdefine TBD
#define RIL_SUPSVCDATASTATUS_TIMEOUT                (0x00000006)      // @constdefine TBD
#define RIL_SUPSVCDATASTATUS_ERROR                  (0x00000007)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Protocol | GPRS Packet Protocols
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GPRSPROTOCOL_UNKNOWN                    (0x00000000)      // @constdefine Unknown
#define RIL_GPRSPROTOCOL_X25                        (0x00000001)      // @constdefine ITU-T/CCITT X.25 Layer 4
#define RIL_GPRSPROTOCOL_IP                         (0x00000002)      // @constdefine Internet Protocol (IETF STD 5)
#define RIL_GPRSPROTOCOL_IHOSP                      (0x00000004)      // @constdefine Internet Hosted Octet Stream Protocol
#define RIL_GPRSPROTOCOL_PPP                        (0x00000008)      // @constdefine Point to Point Protocol
#define RIL_GPRSPROTOCOL_ALL                        (0x0000000f)
// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Protocol | GPRS L2 Protocols
//
// @comm None
//
// See GSM 07.07 10.1.6 for definitions
// -----------------------------------------------------------------------------
//
#define RIL_GPRSL2PROTOCOL_UNKNOWN                  (0x00000000)      // @constdefine
#define RIL_GPRSL2PROTOCOL_NULL                     (0x00000001)      // @constdefine none, for PDP type OSP:IHOSS
#define RIL_GPRSL2PROTOCOL_PPP                      (0x00000002)      // @constdefine Point-to-point protocol for a PDP such as IP
#define RIL_GPRSL2PROTOCOL_PAD                      (0x00000004)      // @constdefine character stream for X.25 character (triple X PAD) mode
#define RIL_GPRSL2PROTOCOL_X25                      (0x00000008)      // @constdefine X.25 L2 (LAPB) for X.25 packet mode
#define RIL_GPRSL2PROTOCOL_ALL                      (0x0000000f)


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Data Comp | GPRS Data Compression
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GPRSDATACOMP_OFF                        (0x00000001)      // @constdefine compression off
#define RIL_GPRSDATACOMP_ON                         (0x00000002)      // @constdefine compression off
#define RIL_GPRSDATACOMP_ALL                        (0x00000003)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Header Comp | GPRS Header Compression
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GPRSHEADERCOMP_OFF                      (0x00000001)    // @constdefine compression off
#define RIL_GPRSHEADERCOMP_ON                       (0x00000002)    // @constdefine compression off
#define RIL_GPRSHEADERCOMP_ALL                      (0x00000003)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Precedence Class | GPRS Precedence Class
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GPRSPRECEDENCECLASS_SUBSCRIBED          (0x00000001)    // @constdefine subscribed value stored in network
#define RIL_GPRSPRECEDENCECLASS_HIGH                (0x00000002)    // @constdefine high priority
#define RIL_GPRSPRECEDENCECLASS_NORMAL              (0x00000004)    // @constdefine normal priority
#define RIL_GPRSPRECEDENCECLASS_LOW                 (0x00000008)    // @constdefine low priority
#define RIL_GPRSPRECEDENCECLASS_ALL                 (0x0000000f)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Delay Class | GPRS Delay Class
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GPRSDELAYCLASS_SUBSCRIBED              (0x00000001)    // @constdefine subscribed value stored in network
#define RIL_GPRSDELAYCLASS_PREDICTIVE1             (0x00000002)    // @constdefine see gsm 02.60
#define RIL_GPRSDELAYCLASS_PREDICTIVE2             (0x00000004)    // @constdefine see gsm 02.60
#define RIL_GPRSDELAYCLASS_PREDICTIVE3             (0x00000008)    // @constdefine see gsm 02.60
#define RIL_GPRSDELAYCLASS_BESTEFFORT              (0x00000010)    // @constdefine see gsm 02.60
#define RIL_GPRSDELAYCLASS_ALL                     (0x0000001f)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Reliability Class | GPRS Reliability Class
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_GPRSRELIABILITYCLASS_SUBSCRIBED        (0x00000001)    // @constdefine subscribed value stored in network
#define RIL_GPRSRELIABILITYCLASS_1                 (0x00000002)    // @constdefine see gsm 03.60
#define RIL_GPRSRELIABILITYCLASS_2                 (0x00000004)    // @constdefine see gsm 03.60
#define RIL_GPRSRELIABILITYCLASS_3                 (0x00000008)    // @constdefine see gsm 03.60
#define RIL_GPRSRELIABILITYCLASS_4                 (0x00000010)    // @constdefine see gsm 03.60
#define RIL_GPRSRELIABILITYCLASS_5                 (0x00000020)    // @constdefine see gsm 03.60
#define RIL_GPRSRELIABILITYCLASS_ALL               (0x0000003f)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Class | GPRS Class
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_GPRSCLASS_UNKNOWN                       (0x00000000)      // @constdefine GPRS class unknown
#define RIL_GPRSCLASS_GSMANDGPRS                    (0x00000001)      // @constdefine Simultaneous voice and GPRS data
#define RIL_GPRSCLASS_GSMORGPRS                     (0x00000002)      // @constdefine Simultaneous voice and GPRS traffic channel, one or other data
#define RIL_GPRSCLASS_GSMORGPRS_EXCLUSIVE           (0x00000004)      // @constdefine Either all voice or all GPRS, both traffic channels unmonitored
#define RIL_GPRSCLASS_GPRSONLY                      (0x00000008)      // @constdefine Only GPRS
#define RIL_GPRSCLASS_GSMONLY                       (0x00000010)      // @constdefine Only circuit switched voice and data
#define RIL_GPRSCLASS_ALL                           (0x0000001f)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Peak Throughput Class | GPRS Peak Throughput Class
//
// @comm Constants represent bits per second
//
// -----------------------------------------------------------------------------
//
#define RIL_PEAKTHRUCLASS_SUBSCRIBED               (0x00000001)    // @constdefine subscribed value stored in network
#define RIL_PEAKTHRUCLASS_8000                     (0x00000002)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_16000                    (0x00000004)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_32000                    (0x00000008)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_64000                    (0x00000010)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_128000                   (0x00000020)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_256000                   (0x00000040)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_512000                   (0x00000080)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_1024000                  (0x00000100)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_2048000                  (0x00000200)    // @constdefine bits per second
#define RIL_PEAKTHRUCLASS_ALL                      (0x000003ff)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS Mean Throughput Class | GPRS Mean Throughput Class
//
// @comm Constants represent octets per hour
//
// -----------------------------------------------------------------------------
//
#define RIL_MEANTHRUCLASS_SUBSCRIBED               (0x00000001)    // @constdefine subscribed value stored in network
#define RIL_MEANTHRUCLASS_100                      (0x00000002)    // @constdefine 0.22 bits/second
#define RIL_MEANTHRUCLASS_200                      (0x00000004)    // @constdefine 0.44 bits/second
#define RIL_MEANTHRUCLASS_500                      (0x00000008)    // @constdefine 1.11 bits/second
#define RIL_MEANTHRUCLASS_1000                     (0x00000010)    // @constdefine 2.2 bits/second
#define RIL_MEANTHRUCLASS_2000                     (0x00000020)    // @constdefine 4.4 bits/second
#define RIL_MEANTHRUCLASS_5000                     (0x00000040)    // @constdefine 11.1 bits/second
#define RIL_MEANTHRUCLASS_10000                    (0x00000080)    // @constdefine 22 bits/second
#define RIL_MEANTHRUCLASS_20000                    (0x00000100)    // @constdefine 44 bits/second
#define RIL_MEANTHRUCLASS_50000                    (0x00000200)    // @constdefine 111 bits/second
#define RIL_MEANTHRUCLASS_100000                   (0x00000400)    // @constdefine 220 bits/second
#define RIL_MEANTHRUCLASS_200000                   (0x00000800)   // @constdefine 440 bits/second
#define RIL_MEANTHRUCLASS_500000                   (0x00001000)   // @constdefine 1,110 bits/second
#define RIL_MEANTHRUCLASS_1000000                  (0x00002000)   // @constdefine 2,200 bits/second
#define RIL_MEANTHRUCLASS_2000000                  (0x00004000)   // @constdefine 4,400 bits/second
#define RIL_MEANTHRUCLASS_5000000                  (0x00008000)   // @constdefine 11,100 bits/second
#define RIL_MEANTHRUCLASS_10000000                 (0x00010000)   // @constdefine 22,000 bits/second
#define RIL_MEANTHRUCLASS_20000000                 (0x00020000)   // @constdefine 44,000 bits/second
#define RIL_MEANTHRUCLASS_50000000                 (0x00040000)   // @constdefine 111,000 bits/second
#define RIL_MEANTHRUCLASS_DONTCARE                 (0x80000000)   // @constdefine best effort
#define RIL_MEANTHRUCLASS_ALL                      (0x8007ffff)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Complete Call Busy | Special value for all CCBS
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_CCBS_ALL                                  (0xffffffff)      // @constdefine All CCBS

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants GPRS SMS | Mobile Originated SMS Service Constants
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_MOSMSSERVICE_CIRCUIT                      (0x00000001)    // @constdefine circuit switched
#define RIL_MOSMSSERVICE_GPRS                         (0x00000002)    // @constdefine GPRS
#define RIL_MOSMSSERVICE_CIRCUITPREFERRED             (0x00000004)    // @constdefine use both, circuit switched preferred
#define RIL_MOSMSSERVICE_GPRSPREFERRED                (0x00000008)    // @constdefine use both, GPRS preferred
#define RIL_MOSMSSERVICE_ALL                          (0x0000000f)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Password type | PIN or PUK password
//
// @comm Used to distiguish between a password that is a PIN vs PUK for RIL_ChangeLockingPassword
//
// -----------------------------------------------------------------------------
#define RIL_PASSWORDTYPE_PIN                        (0x00000001)      // @constdefine The password type is a SIM PIN (editable password)
#define RIL_PASSWORDTYPE_PUK                        (0x00000002)      // @constdefine The password type is a SIM PUK (Non-user editable)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants System Capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_SYSTEMCAPS_NONE                         (0x00000000)      // @constdefine The system does not support any special capabilities.
#define RIL_SYSTEMCAPS_VOICEDATA                    (0x00000001)      // @constdefine The system supports simultaneous voice+Data
#define RIL_SYSTEMCAPS_ALL                          (0x00000001)      // @constdefine The system supports all special capabilities.

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Type | Capability types
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPSTYPE_DIAL                           (0x00000001)      // @constdefine TBD
#define RIL_CAPSTYPE_DTMFDURATIONRANGE              (0x00000002)      // @constdefine TBD
#define RIL_CAPSTYPE_CALLMGTCMDS                    (0x00000003)      // @constdefine TBD
#define RIL_CAPSTYPE_BEARERSERVICE                  (0x00000004)      // @constdefine TBD
#define RIL_CAPSTYPE_RLP                            (0x00000005)      // @constdefine TBD
#define RIL_CAPSTYPE_EQUIPMENTSTATES                (0x00000006)      // @constdefine TBD
#define RIL_CAPSTYPE_PBSTORELOCATIONS               (0x00000007)      // @constdefine TBD
#define RIL_CAPSTYPE_PBINDEXRANGE                   (0x00000008)      // @constdefine TBD
#define RIL_CAPSTYPE_PBENTRYLENGTH                  (0x00000009)      // @constdefine TBD
#define RIL_CAPSTYPE_MSGSERVICETYPES                (0x0000000a)      // @constdefine TBD
#define RIL_CAPSTYPE_MSGMEMORYLOCATIONS             (0x0000000b)      // @constdefine TBD
#define RIL_CAPSTYPE_BROADCASTMSGLANGS              (0x0000000c)      // @constdefine TBD
#define RIL_CAPSTYPE_MSGCONFIGINDEXRANGE            (0x0000000d)      // @constdefine TBD
#define RIL_CAPSTYPE_MSGSTATUSVALUES                (0x0000000e)      // @constdefine TBD
#define RIL_CAPSTYPE_PREFOPINDEXRANGE               (0x0000000f)      // @constdefine TBD
#define RIL_CAPSTYPE_LOCKFACILITIES                 (0x00000010)      // @constdefine TBD
#define RIL_CAPSTYPE_LOCKINGPWDLENGTHS              (0x00000011)      // @constdefine TBD
#define RIL_CAPSTYPE_BARRTYPES                      (0x00000012)      // @constdefine TBD
#define RIL_CAPSTYPE_BARRINGPWDLENGTHS              (0x00000013)      // @constdefine TBD
#define RIL_CAPSTYPE_FORWARDINGREASONS              (0x00000014)      // @constdefine TBD
#define RIL_CAPSTYPE_INFOCLASSES                    (0x00000015)      // @constdefine TBD
#define RIL_CAPSTYPE_HSCSD                          (0x00000016)      // @constdefine TBD
#define RIL_CAPSTYPE_SIMTOOLKITNOTIFICATIONS        (0x00000017)      // @constdefine TBD
#define RIL_CAPSTYPE_GPRSCLASS                      (0x00000018)      // @constdefine TBD
#define RIL_CAPSTYPE_GPRSCONTEXT                    (0x00000019)      // @constdefine TBD
#define RIL_CAPSTYPE_GPRSQOS                        (0x0000001a)      // @constdefine TBD
#define RIL_CAPSTYPE_GPRSQOSMIN                     (0x0000001b)      // @constdefine TBD
#define RIL_CAPSTYPE_GPRSMOSMS                      (0x0000001c)      // @constdefine TBD
#define RIL_CAPSTYPE_DATACOMPRESSION                (0x0000001d)      // @constdefine TBD
#define RIL_CAPSTYPE_ERRORCORRECTION                (0x0000001e)      // @constdefine TBD
#define RIL_CAPSTYPE_SIGNALQUALITYIMPLEMENTATION    (0x0000001f)      // @constdefine TBD
#define RIL_CAPSTYPE_SIMSUPPORT                     (0x00000020)      // @constdefine TBD
#define RIL_CAPSTYPE_GPRSPACKETSUPPORT              (0x00000021)      // @constdefine TBD
#define RIL_CAPSTYPE_CALLPROGRESSNOTIFICATION    (0x00000022)       // @constdefine TBD

#define RIL_CAPSTYPE_ARG_SMALLEST                   RIL_CAPSTYPE_DIAL
#define RIL_CAPSTYPE_ARG_LARGEST                    RIL_CAPSTYPE_CALLPROGRESSNOTIFICATION

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Call Type | Call type capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_CAPS_CALLTYPE_VOICE                     (0x00000001)      // @constdefine TBD
#define RIL_CAPS_CALLTYPE_DATA                      (0x00000002)      // @constdefine TBD
#define RIL_CAPS_CALLTYPE_FAX                       (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Dialing Option | Dialing options capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_DIALOPT_RESTRICTID                 (RIL_DIALOPT_RESTRICTID)    // @constdefine TBD
#define RIL_CAPS_DIALOPT_PRESENTID                  (RIL_DIALOPT_PRESENTID)     // @constdefine TBD
#define RIL_CAPS_DIALOPT_CLOSEDGROUP                (RIL_DIALOPT_CLOSEDGROUP)   // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Call Mgmt | Call management command capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_CALLCMD_RELEASEHELD                (0x00000001)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_RELEASEACTIVE_ACCEPTHELD   (0x00000002)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_RELEASECALL                (0x00000004)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_HOLDACTIVE_ACCEPTHELD      (0x00000008)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_HOLDALLBUTONE              (0x00000010)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_ADDHELDTOCONF              (0x00000020)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_ADDHELDTOCONF_DISCONNECT   (0x00000040)      // @constdefine TBD
#define RIL_CAPS_CALLCMD_INVOKECCBS                 (0x00000080)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Bearer Speed1 | Bearer service speed capabilities (first set)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_SPEED1_AUTO                        (0x00000001)      // @constdefine TBD
#define RIL_CAPS_SPEED1_300_V21                     (0x00000002)      // @constdefine TBD
#define RIL_CAPS_SPEED1_300_V110                    (0x00000004)      // @constdefine TBD
#define RIL_CAPS_SPEED1_1200_V22                    (0x00000008)      // @constdefine TBD
#define RIL_CAPS_SPEED1_1200_75_V23                 (0x00000010)      // @constdefine TBD
#define RIL_CAPS_SPEED1_1200_V110                   (0x00000020)      // @constdefine TBD
#define RIL_CAPS_SPEED1_1200_V120                   (0x00000040)      // @constdefine TBD
#define RIL_CAPS_SPEED1_2400_V22BIS                 (0x00000080)      // @constdefine TBD
#define RIL_CAPS_SPEED1_2400_V26TER                 (0x00000100)      // @constdefine TBD
#define RIL_CAPS_SPEED1_2400_V110                   (0x00000200)      // @constdefine TBD
#define RIL_CAPS_SPEED1_2400_V120                   (0x00000400)      // @constdefine TBD
#define RIL_CAPS_SPEED1_4800_V32                    (0x00000800)      // @constdefine TBD
#define RIL_CAPS_SPEED1_4800_V110                   (0x00001000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_4800_V120                   (0x00002000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_9600_V32                    (0x00004000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_9600_V34                    (0x00008000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_9600_V110                   (0x00010000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_9600_V120                   (0x00020000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_14400_V34                   (0x00040000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_14400_V110                  (0x00080000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_14400_V120                  (0x00100000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_19200_V34                   (0x00200000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_19200_V110                  (0x00400000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_19200_V120                  (0x00800000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_28800_V34                   (0x01000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_28800_V110                  (0x02000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_28800_V120                  (0x04000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_38400_V110                  (0x08000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_38400_V120                  (0x10000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_48000_V110                  (0x20000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_48000_V120                  (0x40000000)      // @constdefine TBD
#define RIL_CAPS_SPEED1_56000_V110                  (0x80000000)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Bearer Speed2 | Bearer service speed capabilities (second set)
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_SPEED2_56000_V120                  (0x00000001)      // @constdefine TBD
#define RIL_CAPS_SPEED2_56000_TRANSP                (0x00000002)      // @constdefine TBD
#define RIL_CAPS_SPEED2_64000_TRANSP                (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Bearer Name | Bearer service name capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_BSVCNAME_DATACIRCUIT_ASYNC_UDI_MODEM   (0x00000001)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_DATACIRCUIT_SYNC_UDI_MODEM    (0x00000002)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_PADACCESS_ASYNC_UDI           (0x00000004)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_PACKETACCESS_SYNC_UDI         (0x00000008)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_DATACIRCUIT_ASYNC_RDI         (0x00000010)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_DATACIRCUIT_SYNC_RDI          (0x00000020)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_PADACCESS_ASYNC_RDI           (0x00000040)      // @constdefine TBD
#define RIL_CAPS_BSVCNAME_PACKETACCESS_SYNC_RDI         (0x00000080)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Bearer CE | Bearer service connection element capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_BSVCCE_TRANSPARENT                 (0x00000001)      // @constdefine TBD
#define RIL_CAPS_BSVCCE_NONTRANSPARENT              (0x00000002)      // @constdefine TBD
#define RIL_CAPS_BSVCCE_BOTH_TRANSPARENT            (0x00000004)      // @constdefine TBD
#define RIL_CAPS_BSVCCE_BOTH_NONTRANSPARENT         (0x00000008)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Equipment | Equipment state capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_EQSTATE_MINIMUM                    (0x00000001)      // @constdefine TBD
#define RIL_CAPS_EQSTATE_FULL                       (0x00000002)      // @constdefine TBD
#define RIL_CAPS_EQSTATE_DISABLETX                  (0x00000004)      // @constdefine TBD
#define RIL_CAPS_EQSTATE_DISABLERX                  (0x00000008)      // @constdefine TBD
#define RIL_CAPS_EQSTATE_DISABLETXANDRX             (0x00000010)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Phonebook | Phonebook storage location capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_PBLOC_SIMEMERGENCY                 (0x00000001)      // @constdefine TBD
#define RIL_CAPS_PBLOC_SIMFIXDIALING                (0x00000002)      // @constdefine TBD
#define RIL_CAPS_PBLOC_SIMLASTDIALING               (0x00000004)      // @constdefine TBD
#define RIL_CAPS_PBLOC_OWNNUMBERS                   (0x00000008)      // @constdefine TBD
#define RIL_CAPS_PBLOC_SIMPHONEBOOK                 (0x00000010)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Message Service | Message service type capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_MSGSVCTYPE_PHASE2                  (0x00000001)      // @constdefine TBD
#define RIL_CAPS_MSGSVCTYPE_PHASE2PLUS              (0x00000002)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Message Storage | Message storage location capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_MSGLOC_BROADCAST                   (0x00000001)      // @constdefine TBD
#define RIL_CAPS_MSGLOC_SIM                         (0x00000002)      // @constdefine TBD
#define RIL_CAPS_MSGLOC_STATUSREPORT                (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps DCS Language | Message broadcast data coding scheme language capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_DCSLANG_GERMAN                     RIL_DCSLANG_GERMAN     // @constdefine TBD
#define RIL_CAPS_DCSLANG_ENGLISH                    RIL_DCSLANG_ENGLISH    // @constdefine TBD
#define RIL_CAPS_DCSLANG_ITALIAN                    RIL_DCSLANG_ITALIAN    // @constdefine TBD
#define RIL_CAPS_DCSLANG_FRENCH                     RIL_DCSLANG_FRENCH     // @constdefine TBD
#define RIL_CAPS_DCSLANG_SPANISH                    RIL_DCSLANG_SPANISH    // @constdefine TBD
#define RIL_CAPS_DCSLANG_DUTCH                      RIL_DCSLANG_DUTCH      // @constdefine TBD
#define RIL_CAPS_DCSLANG_SWEDISH                    RIL_DCSLANG_SWEDISH    // @constdefine TBD
#define RIL_CAPS_DCSLANG_DANISH                     RIL_DCSLANG_DANISH     // @constdefine TBD
#define RIL_CAPS_DCSLANG_PORTUGUESE                 RIL_DCSLANG_PORTUGUESE // @constdefine TBD
#define RIL_CAPS_DCSLANG_FINNISH                    RIL_DCSLANG_FINNISH    // @constdefine TBD
#define RIL_CAPS_DCSLANG_NORWEGIAN                  RIL_DCSLANG_NORWEGIAN  // @constdefine TBD
#define RIL_CAPS_DCSLANG_GREEK                      RIL_DCSLANG_GREEK      // @constdefine TBD
#define RIL_CAPS_DCSLANG_TURKISH                    RIL_DCSLANG_TURKISH    // @constdefine TBD
#define RIL_CAPS_DCSLANG_HUNGARIAN                  RIL_DCSLANG_HUNGARIAN  // @constdefine TBD
#define RIL_CAPS_DCSLANG_POLISH                     RIL_DCSLANG_POLISH     // @constdefine TBD
#define RIL_CAPS_DCSLANG_CZECH                      RIL_DCSLANG_CZECH      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Message Status | Message status capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_MSGSTATUS_RECUNREAD                (0x00000001)      // @constdefine TBD
#define RIL_CAPS_MSGSTATUS_RECREAD                  (0x00000002)      // @constdefine TBD
#define RIL_CAPS_MSGSTATUS_STOUNSENT                (0x00000004)      // @constdefine TBD
#define RIL_CAPS_MSGSTATUS_STOSENT                  (0x00000008)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps SIM | SIM capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_SIM_NONE                  (0x00000000)      // @constdefine TBD
#define RIL_CAPS_SIM_BASIC                 (0x00000001)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Phone Lock | Locking faciliy capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_LOCKFACILITY_NONE                  (0x00000000)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_CNTRL                 (0x00000001)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_PH_SIM                (0x00000002)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_PH_FSIM               (0x00000004)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_SIM                   (0x00000008)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_SIM_PIN2              (0x00000010)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_SIM_FIXEDIALING       (0x00000020)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_NETWORKPERS           (0x00000040)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_NETWORKSUBPERS        (0x00000080)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_SERVICEPROVPERS       (0x00000100)      // @constdefine TBD
#define RIL_CAPS_LOCKFACILITY_CORPPERS              (0x00000200)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Call Barr | Call barring capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_BARRTYPE_ALLOUTGOING               (0x00000001)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_OUTGOINGINT               (0x00000002)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_OUTGOINGINTEXTOHOME       (0x00000004)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_ALLINCOMING               (0x00000008)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_INCOMINGROAMING           (0x00000010)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_INCOMINGNOTINSIM          (0x00000020)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_ALLBARRING                (0x00000040)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_ALLOUTGOINGBARRING        (0x00000080)      // @constdefine TBD
#define RIL_CAPS_BARRTYPE_ALLINCOMINGBARRING        (0x00000100)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Forwarding | Forwarding reason capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_FWDREASON_UNCONDITIONAL            (0x00000001)      // @constdefine TBD
#define RIL_CAPS_FWDREASON_MOBILEBUSY               (0x00000002)      // @constdefine TBD
#define RIL_CAPS_FWDREASON_NOREPLY                  (0x00000004)      // @constdefine TBD
#define RIL_CAPS_FWDREASON_UNREACHABLE              (0x00000008)      // @constdefine TBD
#define RIL_CAPS_FWDREASON_ALLFORWARDING            (0x00000010)      // @constdefine TBD
#define RIL_CAPS_FWDREASON_ALLCONDITIONAL           (0x00000020)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Info Class | Telephony information class capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_INFOCLASS_VOICE                    (RIL_INFOCLASS_VOICE)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_DATA                     (RIL_INFOCLASS_DATA)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_FAX                      (RIL_INFOCLASS_FAX)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_SMS                      (RIL_INFOCLASS_SMS)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_DATACIRCUITSYNC          (RIL_INFOCLASS_DATACIRCUITSYNC)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_DATACIRCUITASYNC         (RIL_INFOCLASS_DATACIRCUITASYNC)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_PACKETACCESS             (RIL_INFOCLASS_PACKETACCESS)     // @constdefine TBD
#define RIL_CAPS_INFOCLASS_PADACCESS                (RIL_INFOCLASS_PADACCESS)     // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps HSCSD Traffic Channel | HSCSD traffic channel coding capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_HSCSDCODING_4800_FULLRATE          (RIL_HSCSDCODING_4800_FULLRATE)     // @constdefine TBD
#define RIL_CAPS_HSCSDCODING_9600_FULLRATE          (RIL_HSCSDCODING_9600_FULLRATE)     // @constdefine TBD
#define RIL_CAPS_HSCSDCODING_14400_FULLRATE         (RIL_HSCSDCODING_14400_FULLRATE)    // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps HSCSD Air Interface | HSCSD air interface user rate capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_HSCSDAIURATE_9600                  (0x00000001)      // @constdefine TBD
#define RIL_CAPS_HSCSDAIURATE_14400                 (0x00000002)      // @constdefine TBD
#define RIL_CAPS_HSCSDAIURATE_19200                 (0x00000004)      // @constdefine TBD
#define RIL_CAPS_HSCSDAIURATE_28800                 (0x00000008)      // @constdefine TBD
#define RIL_CAPS_HSCSDAIURATE_38400                 (0x00000010)      // @constdefine TBD
#define RIL_CAPS_HSCSDAIURATE_43200                 (0x00000020)      // @constdefine TBD
#define RIL_CAPS_HSCSDAIURATE_57600                 (0x00000040)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps SIM Toolkit | SIM Toolkit notification capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CAPS_NOTIFY_SIMTOOLKITCMD               (0x00000001)      // @constdefine TBD
#define RIL_CAPS_NOTIFY_SIMTOOLKITCALLSETUP         (0x00000002)      // @constdefine TBD
#define RIL_CAPS_NOTIFY_SIMTOOLKITEVENT             (0x00000004)      // @constdefine TBD

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Caps Signal Implemetation Quality | Signal Quality Implemetation Capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define RIL_CAPS_SIGNALQUALITY_NOTIFICATION         (0x00000001)      // @constdefine The Radio Module can deliver unsolicited Signal Quality Notifications
#define RIL_CAPS_SIGNALQUALITY_POLLING              (0x00000002)      // @constdefine The Higher layers can poll the radio module in order to get the Signal Quality


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants Maximum lengths | Maximum lengths for string parameters
//
// @comm None
//
// -----------------------------------------------------------------------------
//
#define MAXLENGTH_ADDRESS                           (256)     // @constdefine 256
#define MAXLENGTH_SUBADDR                           (256)     // @constdefine 256
#define MAXLENGTH_DESCRIPTION                       (256)     // @constdefine 256
#define MAXLENGTH_OPERATOR                          (32)      // @constdefine 32
#define MAXLENGTH_OPERATOR_LONG                     (32)      // @constdefine 32
#define MAXLENGTH_OPERATOR_SHORT                    (16)      // @constdefine 16
#define MAXLENGTH_OPERATOR_NUMERIC                  (16)      // @constdefine 16
#define MAXLENGTH_OPERATOR_COUNTRY_CODE             (8)       // @constdefine 8
#define MAXLENGTH_SERVCTR                           (256)     // @constdefine 256
#define MAXLENGTH_PASSWORD                          (256)     // @constdefine 256
#define MAXLENGTH_ERRSHORT                          (256)     // @constdefine 256
#define MAXLENGTH_ERRLONG                           (256)     // @constdefine 256
#define MAXLENGTH_EQUIPINFO                         (128)     // @constdefine 128
#define MAXLENGTH_PHONEBOOKADDR                     (256)     // @constdefine 256
#define MAXLENGTH_PHONEBOOKTEXT                     (256)     // @constdefine 256
#define MAXLENGTH_CURRENCY                          (256)     // @constdefine 256
#define MAXLENGTH_AREAID                            (256)     // @constdefine 256
#define MAXLENGTH_CELLID                            (256)     // @constdefine 256
#define MAXLENGTH_HDR                               (256)     // @constdefine 256
#define MAXLENGTH_MSG                               (512)     // @constdefine 512
#define MAXLENGTH_CMD                               (256)     // @constdefine 256
#define MAXLENGTH_MSGIDS                            (256)     // @constdefine 256
#define MAXLENGTH_USERID                            (256)     // @constdefine 256
#define MAXLENGTH_DTMF                              (256)     // @constdefine 256
#define MAXLENGTH_GPRSADDRESS                       (64)      // @constdefine 64
#define MAXLENGTH_GPRSACCESSPOINTNAME               (64)      // @constdefine 64
#define MAXLENGTH_BCCH                              (48)      // @constdefine 48
#define MAXLENGTH_NMR                               (16)      // @constdefine 16
#define MAXLENGTH_ATR                               (33)      // @constdefine 33
#define MAXLENGTH_RADIOLOG                          (128)     // @constdefine 128
//
// Data types
//

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILADDRESS | Represents a phone number
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct riladdress_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwType;                           // @field type of number
    DWORD dwNumPlan;                        // @field numbering plan
    WCHAR wszAddress[MAXLENGTH_ADDRESS];    // @field address (min 3, max 43)
} RILADDRESS, *LPRILADDRESS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSUBADDRESS | The subaddress of a called party
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsubaddress_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwType;                           // @field type of subaddress
    WCHAR wszSubAddress[MAXLENGTH_SUBADDR]; // @field subaddress (min 2, max 23)
} RILSUBADDRESS, *LPRILSUBADDRESS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSERIALPORTSTATS | Statistics of the virtual serial port
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilserialportstats_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwReadBitsPerSecond;              // @field bit rate for reading data
    DWORD dwWrittenBitsPerSecond;           // @field bit rate for writing data
} RILSERIALPORTSTATS, *LPRILSERIALPORTSTATS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSUBSCRIBERINFO | A phone number assigned to the user
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsubscriberinfo_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    RILADDRESS raAddress;                   // @field the assigned address
    WCHAR wszDescription[MAXLENGTH_DESCRIPTION]; // @field text relating to this subscriber
    DWORD dwSpeed;                          // @field data rate related to this number
    DWORD dwService;                        // @field the service related to this number
    DWORD dwITC;                            // @field information transfer capability
    DWORD dwAddressId;                      // @field the address ID of this number
} RILSUBSCRIBERINFO, *LPRILSUBSCRIBERINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILOPERATORNAMES | The different representations of an operator
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct riloperatornames_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    char szLongName[MAXLENGTH_OPERATOR_LONG];   // @field long representation (max 16 characters)
    char szShortName[MAXLENGTH_OPERATOR_SHORT]; // @field short representation (max 8 characters)
    char szNumName[MAXLENGTH_OPERATOR_NUMERIC]; // @field numeric representation (3 digit country code & 2 digit network code)
    char szCountryCode[MAXLENGTH_OPERATOR_COUNTRY_CODE]; // @field 2 character ISO 3166 country repesentation of the MCC
} RILOPERATORNAMES, *LPRILOPERATORNAMES;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILOPERATORINFO | Indicates status of a particular operator
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct riloperatorinfo_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwIndex;                          // @field index, if applicable
    DWORD dwStatus;                         // @field registration status, if applicable
    RILOPERATORNAMES ronNames;              // @field representations of an operator
} RILOPERATORINFO, *LPRILOPERATORINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCALLERIDSETTINGS | Caller ID settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcalleridsettings_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwStatus;                         // @field activation status
    DWORD dwProvisioning;                   // @field network provisioning status
} RILCALLERIDSETTINGS, *LPRILCALLERIDSETTINGS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILHIDEIDSETTINGS | Hide ID settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilhideidsettings_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwStatus;                         // @field activation status
    DWORD dwProvisioning;                   // @field network provisioning status
} RILHIDEIDSETTINGS, *LPRILHIDEIDSETTINGS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILDIALEDIDSETTINGS | Dialed ID settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rildialedidsettings_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwStatus;                         // @field activation status
    DWORD dwProvisioning;                   // @field network provisioning status
} RILDIALEDIDSETTINGS, *LPRILDIALEDIDSETTINGS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILHIDECONNECTEDIDSETTINGS | Hide Connected ID settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilhideconnectedidsettings_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwStatus;                         // @field activation status
    DWORD dwProvisioning;                   // @field network provisioning status
} RILHIDECONNECTEDIDSETTINGS, *LPRILHIDECONNECTEDIDSETTINGS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCLOSEDGROUPSETTINGS | Close user group settings
//
// @comm This feature is not used and is untested.
//
// -----------------------------------------------------------------------------
typedef struct rilclosedgroupsettings_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwStatus;                         // @field activation status
    DWORD dwIndex;                          // @field CUG index
    DWORD dwInfo;                           // @field additional CUG flags
} RILCLOSEDGROUPSETTINGS, *LPRILCLOSEDGROUPSETTINGS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCALLFORWARDINGSETTING | Call forwarding service settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcallforwardingsettings_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwStatus;                         // @field activation status
    DWORD dwInfoClasses;                    // @field indicates which classes of calls to forward
    RILADDRESS raAddress;                   // @field forwarding address
    RILSUBADDRESS rsaSubAddress;            // @field forwarding subaddress
    DWORD dwDelayTime;                      // @field seconds to wait in <def RIL_FWDREASON_NOREPLY> case
} RILCALLFORWARDINGSETTINGS, *LPRILCALLFORWARDINGSETTINGS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCALLINFO | Information about a specific call
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcallinfo_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwID;                             // @field identifies each call
    DWORD dwDirection;                      // @field incoming or outgoing
    DWORD dwStatus;                         // @field properties of the call
    DWORD dwType;                           // @field voice or data or fax
    DWORD dwMultiparty;                     // @field conference call status
    RILADDRESS raAddress;                   // @field call address
    WCHAR wszDescription[MAXLENGTH_DESCRIPTION];    // @field any associated text
    DWORD dwDisconnectCode;		// if dwStatus is disconnected - this contains the reason
} RILCALLINFO, *LPRILCALLINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGAININFO | Audio gain information
//
// @comm The minimum and maximum values for both dwTxGain and dwRxGain
//       are 0 and ULONG_MAX (that is, 0xFFFFFFFFUL; see limits.h).
//       Values between these extremes scale linearly.
//
//       It is the RIL Driver's responsibility to scale these values
//       to match whatever is appropriate for the corresponding radio.
//       So for example, if a radio's gain range is from 0 to 0x1F,
//       the RIL Driver should interpret 0xFFFFFFFF as 0x1F, and map
//       intermediate values proportionately.
//
// -----------------------------------------------------------------------------
typedef struct rilgaininfo_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwTxGain;                         // @field transmit gain level
    DWORD dwRxGain;                         // @field receive gain level
} RILGAININFO, *LPRILGAININFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILAUDIODEVICEINFO | Audio device information
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilaudiodeviceinfo_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwTxDevice;                       // @field transmit device
    DWORD dwRxDevice;                       // @field receive device
} RILAUDIODEVICEINFO, *LPRILAUDIODEVICEINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILHSCSDINFO | High speed circuit switched data settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilhscsdinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwTranspRxTimeslots;          // @field number of receive timeslots for transparent HSCSD calls
    DWORD dwTranspChannelCodings;       // @field accepted channel codings for transparent HSCSD calls
    DWORD dwNonTranspRxTimeslots;       // @field number of receive timeslots for non-transparent HSCSD calls
    DWORD dwNonTranspChannelCodings;    // @field accepted channel codings for non-transparent HSCSD calls
    DWORD dwAirInterfaceUserRate;       // @field air interface user rate for non-transparent HSCSD calls
    DWORD dwRxTimeslotsLimit;           // @field maximum number of receive timeslots to be used during the next non-transparent HSCSD call
    BOOL fAutoSvcLevelUpgrading;        // @field TRUE if automatic user-initiated service level upgrading for non-transparent HSCSD calls is enabled, FALSE otherwise
} RILHSCSDINFO, *LPRILHSCSDINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCALLHSCSDINFO | High speed circuit switched data information for the current call
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcallhscsdinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwRxTimeslots;                // @field number of receive timeslots currently in use
    DWORD dwTxTimeslots;                // @field number of transmit timeslots currently in use
    DWORD dwAirInterfaceUserRate;       // @field air interface user rate currently in use
    DWORD dwChannelCoding;              // @field current channel coding
} RILCALLHSCSDINFO, *LPRILCALLHSCSDINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILDATACOMPINFO | Data compression settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rildatacompinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwDirection;                  // @field compression in transmit and/or receive direcitons
    DWORD dwNegotiation;                // @field compression is required or optional
    DWORD dwMaxDictEntries;             // @field maximum number of dictionary entries
    DWORD dwMaxStringLength;            // @field maximum string length
} RILDATACOMPINFO, *LPRILDATACOMPINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILERRORCORRECTIONINFO | Error correction settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilerrorcorrectioninfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwOriginalRequest;            // @field TBD
    DWORD dwOriginalFallback;           // @field TBD
    DWORD dwAnswererFallback;           // @field TBD
} RILERRORCORRECTIONINFO, *LPRILERRORCORRECTIONINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILBEARERSVCINFO | Bearer service settings
//
// @comm For <def RIL_BSVCCE_BOTH_> constants, the subsequent text indicates the
//       preferred connection element.  For instance, <def RIL_BSVCCE_BOTH_TRANSPARENT>
//       means that both transparent and non transparent are supported, but transparent
//       is preferred.
//
// -----------------------------------------------------------------------------
typedef struct rilbearersvcinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwSpeed;                      // @field offered data speed (protocol dependant)
    DWORD dwServiceName;                // @field type of data service
    DWORD dwConnectionElement;          // @field indicates transparent or non-transparent connection
} RILBEARERSVCINFO, *LPRILBEARERSVCINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILRLPINFO | Radio link protocol settings
//
// @comm None
//
// -----------------------------------------------------------------------------
//
typedef struct rilrlpinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwIWS;                        // @field IWF-to-MS window size
    DWORD dwMWS;                        // @field MS-to-IWF window size
    DWORD dwAckTimer;                   // @field acknowledgement timer in 10s of milliseconds (T1)
    DWORD dwRetransmissionAttempts;     // @field number of retransmission attempts (N2)
    DWORD dwVersion;                    // @field RLP version number
    DWORD dwResequencingPeriod;         // @field resequencing period (T4)
} RILRLPINFO, *LPRILRLPINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMSGSERVICEINFO | Messaging service settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmsgserviceinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwService;                    // @field supported service types
    DWORD dwMsgClasses;                 // @field supported message classes
    DWORD dwReadLocation;               // @field currect read location
    DWORD dwReadUsed;                   // @field number of fields used
    DWORD dwReadTotal;                  // @field total number of fields
    DWORD dwWriteLocation;              // @field currect read location
    DWORD dwWriteUsed;                  // @field number of fields used
    DWORD dwWriteTotal;                 // @field total number of fields
    DWORD dwStoreLocation;              // @field currect read location
    DWORD dwStoreUsed;                  // @field number of fields used
    DWORD dwStoreTotal;                 // @field total number of fields
} RILMSGSERVICEINFO, *LPRILMSGSERVICEINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMSGDCS | Message data coding scheme
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmsgdcs_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwType;                       // @field DCS type
    DWORD dwFlags;                      // @field DCS flags
    DWORD dwMsgClass;                   // @field message class (Only for RIL_DCSTYPE_GENERAL and RIL_DCSTYPE_MSGCLASS)
    DWORD dwAlphabet;                   // @field DCS alphabet
    DWORD dwIndication;                 // @field indication (Only for RIL_DCSTYPE_MSGWAIT)
    DWORD dwLanguage;                   // @field indication (Only for RIL_DCSTYPE_LANGUAGE)
} RILMSGDCS, *LPRILMSGDCS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILRANGE | Range of values
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilrange_tag{
    DWORD dwMinValue;                   // @field minimum value
    DWORD dwMaxValue;                   // @field maximum value
} RILRANGE, *LPRILRANGE;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMSGCONFIG | Messaging configuration
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmsgconfig_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    RILADDRESS raSvcCtrAddress;         // @field service center address
} RILMSGCONFIG, *LPRILMSGCONFIG;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCBMSGCONFIG | Cell broadcast messaging configuration
//
// @comm None
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilcbmsgconfig_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwBroadcastMsgLangs;          // @field broadcast message languages
    BOOL fAccept;                       // @field TRUE if broadcast message ranges are accepted (vs. rejected)
    RILRANGE rgrrBroadcastMsgIDs[];     // @field an array of RILRANGE IDs to set, a same min/max value specifies a single ID
} RILCBMSGCONFIG, *LPRILCBMSGCONFIG;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMESSAGE | Message data
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmessage_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    RILADDRESS raSvcCtrAddress;         // @field service center address
    DWORD dwType;                       // @field type of message
    DWORD dwFlags;                      // @field message flags
    union {                             // @field UNION MEMBER

        struct {                        // @field RIL_MSGTYPE_IN_DELIVER
            RILADDRESS raOrigAddress;   // @field originating address
            DWORD dwProtocolID;         // @field message protocol
            RILMSGDCS rmdDataCoding;    // @field data coding scheme
            SYSTEMTIME stSCReceiveTime; // @field receive time (UTC)
            DWORD cbHdrLength;          // @field length of header in bytes
            DWORD cchMsgLength;         // @field length of body in bytes
            BYTE rgbHdr[MAXLENGTH_HDR]; // @field header buffer
            BYTE rgbMsg[MAXLENGTH_MSG]; // @field body buffer
        } msgInDeliver;                 // @field End RIL_MSGTYPE_IN_DELIVER

        struct {                        // @field RIL_MSGTYPE_IN_STATUS
            DWORD dwTgtMsgReference;    // @field target message reference
            RILADDRESS raTgtRecipAddress; // @field receipient address
            SYSTEMTIME stTgtSCReceiveTime; // @field receipient receive time (UTC)
            SYSTEMTIME stTgtDischargeTime; // @field receipient dischage time (UTC)
            DWORD dwTgtDlvStatus;       // @field delivery status
            DWORD dwProtocolID;         // @field message protocol
            RILMSGDCS rmdDataCoding;    // @field data coding scheme
            DWORD cbHdrLength;          // @field length of header in bytes
            DWORD cchMsgLength;         // @field length of body in bytes
            BYTE rgbHdr[MAXLENGTH_HDR]; // @field header buffer
            BYTE rgbMsg[MAXLENGTH_MSG]; // @field body buffer
        } msgInStatus;                  // @field End RIL_MSGTYPE_IN_STATUS

        struct {                        // @field RIL_MSGTYPE_OUT_SUBMIT
            RILADDRESS raDestAddress;   // @field destination address
            DWORD dwProtocolID;         // @field message protocol
            RILMSGDCS rmdDataCoding;    // @field data coding scheme
            DWORD dwVPFormat;           // @field TBD
            SYSTEMTIME stVP;            // @field relative validity period (values are expressed relative to the current time)
            DWORD cbHdrLength;          // @field length of header in bytes
            DWORD cchMsgLength;         // @field length of body in bytes
            BYTE rgbHdr[MAXLENGTH_HDR]; // @field header buffer
            BYTE rgbMsg[MAXLENGTH_MSG]; // @field body buffer
        } msgOutSubmit;                 // @field End RIL_MSGTYPE_OUT_SUBMIT

        struct {                        // @field RIL_MSGTYPE_OUT_COMMAND
            DWORD dwProtocolID;         // @field message protocol
            DWORD dwCommandType;        // @field command type
            DWORD dwTgtMsgReference;    // @field target message reference
            RILADDRESS raDestAddress;   // @field destination address
            DWORD cbCmdLength;          // @field length of command in bytes
            BYTE rgbCmd[MAXLENGTH_CMD]; // @field command buffer
        } msgOutCommand;                // @field End RIL_MSGTYPE_OUT_COMMAND

        struct {                        // @field RIL_MSGTYPE_BC_GENERAL
            DWORD dwGeoScope;           // @field message protocol
            DWORD dwMsgCode;            // @field message code
            DWORD dwUpdateNumber;       // @field update number
            DWORD dwID;                 // @field identity
            RILMSGDCS rmdDataCoding;    // @field data coding scheme
            DWORD dwTotalPages;         // @field total number of pages
            DWORD dwPageNumber;         // @field current page number
            DWORD cchMsgLength;         // @field length of message in bytes
            BYTE rgbMsg[MAXLENGTH_MSG]; // @field message buffer
        } msgBcGeneral;                 // @field End RIL_MSGTYPE_BC_GENERAL

        struct {                        // @field RIL_MSGTYPE_OUT_RAW
            DWORD cchMsgLength;         // @field length of body in bytes
            BYTE rgbMsg[MAXLENGTH_MSG]; // @field message buffer
        } msgOutRaw;                    // @field End RIL_MSGTYPE_OUT_RAW

        struct {                                // @field RIL_MSGTYPE_IN_IS637DELIVER
            RILADDRESS      raOrigAddress;      // @field originating address
            RILSUBADDRESS   rsaOrigSubaddr;     // @field
            //There is no digit-mode in incoming message because the driver can convert both of them to ASCII

            SYSTEMTIME      stSCReceiveTime;    // @field (SMSC Timestamp) receive time (UTC)

            SYSTEMTIME  stValidityPeriodAbs;    // @field UTC time
            SYSTEMTIME  stValidityPeriodRel;    // @field Relative time
            SYSTEMTIME  stDeferredDelTimeAbs;   // @field UTC time
            SYSTEMTIME  stDeferredDelTimeRel;   // @field Relative time

            DWORD       dwNumMsgs;              // @field Used for Voicemail only.  Indicates the number of Messages on Vmail
            RILADDRESS  raCallBackNumber;       // @field (Only paging and Text -s) user can give a callback number in certain messages
            DWORD       dwMsgPriority;          // @field RIL_MSGPRIORITY_ constant
            DWORD       dwMsgPrivacy;           // @field RIL_MSGPRIVACYCLASS_ constant

            BOOL    bUserAckRequest;            // @field 0 = Not Requested; 1 = Requested ; This is an ack from the end user
            DWORD   dwMsgDisplayMode;           // @field RIL_MSGDISPLAYMODE_ constant

            DWORD   dwTeleservice;              // @field RIL_MSGTELESERVICE_* Constant[Mandatory]

            DWORD   dwMsgID;                    // @field [Mandatory] Message ID.  (0-65535) (In the WAP architecture each part of a multipart message share the same MsgID)
            DWORD   dwMsgLang;                  // @field Under Investigation
            DWORD   dwMsgEncoding;              // @field RIL_MSGCODING_* constant [5 bits] under Investigation
            DWORD   cchMsgLength;               // @field length of body in bytes
            BYTE    rgbMsg[MAXLENGTH_MSG];      // @field body buffer
        } msgIS637InDeliver;                    // @field End RIL_MSGTYPE_IN_IS637DELIVER

        struct {                                // @field RIL_MSGTYPE_OUT_IS637SUBMIT
            RILADDRESS      raDestAddress;      // @field destination address
            RILSUBADDRESS   rsaDestSubaddr;     // @field destination subaddress
            BOOL            bDigit;             // @field specifies if the address in RILADDRESS is 4bit mode (=0) or in 8 bit mode (=1) (should be set to 1 by default)

            SYSTEMTIME  stValidityPeriodAbs;    // @field UTC time
            SYSTEMTIME  stValidityPeriodRel;    // @field Relative time
            SYSTEMTIME  stDeferredDelTimeAbs;   // @field UTC time
            SYSTEMTIME  stDeferredDelTimeRel;   // @field Relative time

            BOOL    bDeliveryAckRequest;        // @field 0 = Not Requested; 1 = Requested ; This is an delivery ack (no user confirmation)
            BOOL    bUserAckRequest;            // @field 0 = Not Requested; 1 = Requested ; This is an ack from the end user
            BOOL    bBearerReplyRequest;        // @field specifies the bearer reply field is set (technically this can be set, but it should not be) ; Boolean (0=not set, 1=set)
            DWORD   dwReplySeqNumber;           // @field the Seuqence number of the message bing replied to; (typically the MSGID)
            DWORD   dwMsgDisplayMode;           // @field RIL_MSGDISPLAYMODE_* constant

            RILADDRESS  raCallBackNumber;       // @field (Only paging and Text -s) user can give a callback number in certain messages

            DWORD       dwMsgPriority;          // @field RIL_MSGPRIORITY_ constant
            DWORD       dwMsgPrivacy;           // @field RIL_MSGPRIVACYCLASS_ constant

            DWORD   dwTeleservice;              // @field RIL_MSGTELESERVICE_* Constant[Mandatory]

            DWORD   dwMsgID;                    // @field [Mandatory] Message ID.  (0-65535) (In the WAP architecture each part of a multipart message share the same MsgID)
            DWORD   dwMsgLang;                  // @field Under Investigation
            DWORD   dwMsgEncoding;              // @field RIL_MSGCODING_* constant [5 bits] under Investigation
            DWORD   cchMsgLength;               // @field length of body in bytes
            BYTE    rgbMsg[MAXLENGTH_MSG];      // @field body buffer
        } msgIS637OutSubmit;                    // @field End RIL_MSGTYPE_OUT_IS637SUBMIT

        struct {                                // @field RIL_MSGTYPE_IN_IS637STATUS
            RILADDRESS      raOrigAddress;      // @field originating address
            RILSUBADDRESS   rsaOrigSubaddr;     // @field
            // There is no digit-mode in incoming message because the driver can convert both of them to ASCII

            SYSTEMTIME      stSCReceiveTime;    // @field (SMSC Timestamp) receive time (UTC)
            DWORD           dwCauseCode;        // @field Cause_Codes Under Investigation, most likely these will be implemented as RIL errors
            DWORD           dwReplySeqNumber;   // @field The Sequence number of the message bing replied to; (typically the MSGID)
            DWORD           dwUserResponseCode; // @field User Response Code (Carrier Specific Element when responding giving a User Ack)
            DWORD           dwMsgStatusType;    // @field type of status message RIL_MSGSTATUSTYPE_* constant

            DWORD   dwMsgID;                    // @field [Mandatory] Message ID.  (0-65535) (In the WAP architecture each part of a multipart message share the same MsgID)
            DWORD   dwMsgLang;                  // @field Under Investigation
            DWORD   dwMsgEncoding;              // @field RIL_MSGCODING_* constant [5 bits] under Investigation
            DWORD   cchMsgLength;               // @field length of body in bytes
            BYTE    rgbMsg[MAXLENGTH_MSG];      // @field body buffer
        } msgIS637InStatus;                     // @field End RIL_MSGTYPE_IN_IS637STATUS

        struct {                                // @field RIL_MSGTYPE_OUT_IS637STATUS
            RILADDRESS      raDestAddress;      // @field destination address
            RILSUBADDRESS   rsaDestSubaddr;     // @field destination subaddress
            BOOL            bDigit;             // @field specifies if the address in RILADDRESS is 4bit mode (=0) or in 8 bit mode (=1) (should be set to 1 by default)

            DWORD   dwReplySeqNumber;           // @field The Sequence number of the message bing replied to; (typically the MSGID)
            DWORD   dwUserResponseCode;         // @field User Response Code (Carrier Specific Element when responding giving a User Ack)

            DWORD   dwMsgID;                    // @field [Mandatory] Message ID.  (0-65535) (In the WAP architecture each part of a multipart message share the same MsgID)
            DWORD   dwMsgLang;                  // @field Under Investigation
            DWORD   dwMsgEncoding;              // @field RIL_MSGCODING_* constant [5 bits] under Investigation
            DWORD   cchMsgLength;               // @field length of body in bytes
            BYTE    rgbMsg[MAXLENGTH_MSG];      // @field body buffer
        } msgIS637OutStatus;                    // @field End RIL_MSGTYPE_OUT_IS637STATUS

    };

} RILMESSAGE, *LPRILMESSAGE;


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMESSAGE_IN_SIM | Message data in sim info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmessage_in_sim_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;
    DWORD dwLocation;                   // @field storage area (one of RIL_MSGLOC_xxxx)
    DWORD dwIndex;                      // @field storage index occupied by the message
} RILMESSAGE_IN_SIM, *LPRILMESSAGE_IN_SIM;


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMESSAGEINFO | Message data with additional info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmessageinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwIndex;                      // @field storage index occupied by the message
    DWORD dwStatus;                     // @field message status
    RILMESSAGE rmMessage;               // @field the message itself
} RILMESSAGEINFO, *LPRILMESSAGEINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILEQUIPMENTINFO | Equipment info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilequipmentinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    char szManufacturer[MAXLENGTH_EQUIPINFO]; // @field manufacturer of the radio hardware
    char szModel[MAXLENGTH_EQUIPINFO];  // @field model of the radio hardware
    char szRevision[MAXLENGTH_EQUIPINFO]; // @field software version of the radio stack
    char szSerialNumber[MAXLENGTH_EQUIPINFO]; // @field equipment identity (IMEI)
} RILEQUIPMENTINFO, *LPRILEQUIPMENTINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILEQUIPMENTSTATE | Equipment state
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilequipmentstate_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwRadioSupport;               // @field RIL_RADIOSUPPORT_* Parameter
    DWORD dwEqState;                    // @field RIL_EQSTATE_* Parameter
    DWORD dwReadyState;                 // @field RIL_READYSTATE_* Parameter
} RILEQUIPMENTSTATE, *LPRILEQUIPMENTSTATE;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILPHONEBOOKINFO | Phonebook settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilphonebookinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwStoreLocation;              // @field location of phonebook memory
    DWORD dwUsed;                       // @field number of locations used
    DWORD dwTotal;                      // @field total number of phonebook locations
} RILPHONEBOOKINFO, *LPRILPHONEBOOKINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILPHONEBOOKENTRY | A single phonebook entry
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilphonebookentry_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwIndex;                      // @field index of the entry
    RILADDRESS raAddress;               // @field the stored address
    WCHAR wszText[MAXLENGTH_PHONEBOOKTEXT]; // @field assciated text
} RILPHONEBOOKENTRY, *LPRILPHONEBOOKENTRY;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILATRINFO | Answer to Reset information
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilatrinfo_tag
{
    DWORD cbSize;
    DWORD dwParams;
    DWORD dwPhase;
    DWORD cbATRSize;
    BYTE rgbATR[MAXLENGTH_ATR];
} RILATRINFO, *LPRILATRINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMTOOLKITEVENTCAPS | SIM TOOLKIT EVENT LIST CAPABILITIES
//
// @comm This structure indicates who implements the various SIM ToolKit Events
//
// -----------------------------------------------------------------------------
typedef struct rilsimtoolkiteventcaps_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwMTCall;                     // @constdefine TBD
    DWORD dwCallConnected;              // @constdefine TBD
    DWORD dwCallDisconnected;           // @constdefine TBD
    DWORD dwLocationStatus;             // @constdefine TBD
    DWORD dwUserActivity;               // @constdefine TBD
    DWORD dwIdleScreen;                 // @constdefine TBD
    DWORD dwLanguageSelection;          // @constdefine TBD
    DWORD dwBrowserTermination;         // @constdefine TBD
    DWORD dwDataAvailable;              // @constdefine TBD
    DWORD dwChannelStatus;              // @constdefine TBD
    DWORD dwDisplayChange;              // @constdefine TBD
} RILSIMTOOLKITEVENTCAPS, *LPRILSIMTOOLKITEVENTCAPS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMTOOLKITNOTIFYCAPS | SIM TOOLKIT NOTIFY CAPABILITIES
//
// @comm This structure indicates who implements the various SIM ToolKit Notifications
//
// -----------------------------------------------------------------------------
typedef struct rilsimtoolkitnotifycaps_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwRefresh;                    // @constdefine TBD
    DWORD dwMoreTime;                   // @constdefine TBD
    DWORD dwPollInterval;               // @constdefine TBD
    DWORD dwPollingOff;                 // @constdefine TBD
    DWORD dwSetUpCall;                  // @constdefine TBD
    DWORD dwSendSS;                     // @constdefine TBD
    DWORD dwSendUSSD;                   // @constdefine TBD
    DWORD dwSendSMS;                    // @constdefine TBD
    DWORD dwPlayTone;                   // @constdefine TBD
    DWORD dwDisplayText;                // @constdefine TBD
    DWORD dwGetInkey;                   // @constdefine TBD
    DWORD dwGetInput;                   // @constdefine TBD
    DWORD dwSelectItem;                 // @constdefine TBD
    DWORD dwSetupMenu;                  // @constdefine TBD
    DWORD dwSetupIdleModeText;          // @constdefine TBD
    DWORD dwLocalInfo;                  // @constdefine TBD
    DWORD dwNotifyFlags;                // @combination of RIL_CAPS_NOTIFY_* flags
    DWORD dwSetupEventList;             // @constdefine TBD
    DWORD dwSendDTMF;                   // @constdefine TBD
    DWORD dwLaunchBrowser;              // @constdefine TBD
    DWORD dwOpenChannel;                // @constdefine TBD
    DWORD dwCloseChannel;               // @constdefine TBD
    DWORD dwReceiveData;                // @constdefine TBD
    DWORD dwSendData;                   // @constdefine TBD
    DWORD dwTimerManagement;            // @constdefine TBD
    DWORD dwRunAtCmd;                   // @constdefine TBD
    RILSIMTOOLKITEVENTCAPS rstecEvents; // @constdefine TBD
} RILSIMTOOLKITNOTIFYCAPS, *LPRILSIMTOOLKITNOTIFYCAPS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMTOOLKITCMD | SIM toolkit command details.

//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsimtoolkitcmd_tag
{
    DWORD cbSize;
    DWORD dwParams;
    DWORD dwId;
    DWORD dwTag;
    DWORD dwType;
    DWORD dwQualifier;
    DWORD dwError;
    DWORD dwDetailsOffset;
    DWORD dwDetailsSize;
} RILSIMTOOLKITCMD;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMTOOLKITRSP | Response to a SIM toolkit command.

//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsimtoolkitrsp_tag
{
    DWORD cbSize;
    DWORD dwParams;
    DWORD dwId;
    DWORD dwTag;
    DWORD dwType;
    DWORD dwQualifier;
    DWORD dwResponse;
    DWORD dwAdditionalInfo;
} RILSIMTOOLKITRSP;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMCMDPARAMETERS | Parameters for a restricted SIM command

//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsimcmdparameters_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwFileID;                     // @field SIM file ID
    DWORD dwParameter1;                 // @field parameter specific to SIM command
    DWORD dwParameter2;                 // @field parameter specific to SIM command
    DWORD dwParameter3;                 // @field parameter specific to SIM command
} RILSIMCMDPARAMETERS, *LPRILSIMCMDPARAMETERS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMRESPONSE | Response to a restrcited SIM command
//
// @comm None
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilsimresponse_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwStatusWord1;                // @field return parameter specific to SIM command
    DWORD dwStatusWord2;                // @field return parameter specific to SIM command
    BYTE pbResponse[];                  // @field additional bytes of response data
} RILSIMRESPONSE, *LPRILSIMRESPONSE;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIMRECORDSTATUS | Response to a restrcited SIM command
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsimrecordstatus_tag {
    DWORD cbSize;                           // @field Size of the structure in bytes
    DWORD dwParams;                         // @field Indicates valid parameter values
    DWORD dwRecordType;                     // @field RIL_SIMRECORDTYPE_* Constant
    DWORD dwItemCount;                      // @field Number of items in the record
    DWORD dwSize;                           // @field Size in bytes of each item
} RILSIMRECORDSTATUS, *LPRILSIMRECORDSTATUS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCOSTINFO | Service cost info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcostinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwCCM;                        // @field current call meter
    DWORD dwACM;                        // @field accumulated call meter
    DWORD dwMaxACM;                     // @field maximum accumulated call meter
    DWORD dwCostPerUnit;                // @field cost per unit, in 16.16 fixed point
    WCHAR wszCurrency[MAXLENGTH_CURRENCY]; // @field current currency
} RILCOSTINFO, *LPRILCOSTINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSIGNALQUALITY | Signal quality info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilsignalquality_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    int nSignalStrength;                // @field TBD
    int nMinSignalStrength;             // @field TBD
    int nMaxSignalStrength;             // @field TBD
    DWORD dwBitErrorRate;               // @field bit error rate in 1/100 of a percent
    int nLowSignalStrength;             // @field TBD
    int nHighSignalStrength;            // @field TBD
} RILSIGNALQUALITY, *LPRILSIGNALQUALITY;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCELLTOWERINFO | Cell tower info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcelltowerinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwMobileCountryCode;          // @field TBD
    DWORD dwMobileNetworkCode;          // @field TBD
    DWORD dwLocationAreaCode;           // @field TBD
    DWORD dwCellID;                     // @field TBD
    DWORD dwBaseStationID;              // @field TBD
    DWORD dwBroadcastControlChannel;    // @field TBD
    DWORD dwRxLevel;                    // @field Value from 0-63 (see GSM 05.08, 8.1.4)
    DWORD dwRxLevelFull;                // @field Value from 0-63 (see GSM 05.08, 8.1.4)
    DWORD dwRxLevelSub;                 // @field Value from 0-63 (see GSM 05.08, 8.1.4)
    DWORD dwRxQuality;                  // @field Value from 0-7  (see GSM 05.08, 8.2.4)
    DWORD dwRxQualityFull;              // @field Value from 0-7  (see GSM 05.08, 8.2.4)
    DWORD dwRxQualitySub;               // @field Value from 0-7  (see GSM 05.08, 8.2.4)
    DWORD dwIdleTimeSlot;               // @field TBD
    DWORD dwTimingAdvance;              // @field TBD
    DWORD dwGPRSCellID;                 // @field TBD
    DWORD dwGPRSBaseStationID;          // @field TBD
    DWORD dwNumBCCH;                    // @field TBD
    BYTE rgbBCCH[MAXLENGTH_BCCH];       // @field TBD
    BYTE rgbNMR[MAXLENGTH_NMR];         // @field TBD
} RILCELLTOWERINFO, *LPRILCELLTOWERINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILREMOTEPARTYINFO | Incoming call info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilremotepartyinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    RILADDRESS raAddress;               // @field address of caller
    RILSUBADDRESS rsaSubAddress;        // @field subaddress of caller
    WCHAR wszDescription[MAXLENGTH_DESCRIPTION]; // @field text associated with caller
    DWORD dwValidity;                   // @field indicates validity of caller info
} RILREMOTEPARTYINFO, *LPRILREMOTEPARTYINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCALLWAITINGINFO | Call waiting info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcallwaitinginfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwCallType;                   // @field type of call
    DWORD dwAddressId;                  // @field indicates address ID on which the incoming call arrived (if available)
    RILREMOTEPARTYINFO rrpiCallerInfo;  // @field caller information
} RILCALLWAITINGINFO, *LPRILCALLWAITINGINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILINTERMEDIATESSINFO | Intermediate Supplemenary Service Info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilintermediatessinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwNotificationCode;           // @field indicates type of notification
    DWORD dwCallUserGroupIndex;         // @field indicates the CUG Index
} RILINTERMEDIATESSINFO, *LPRILINTERMEDIATESSINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILUNSOLICITEDSSINFO | Unsolicited Supplemenary Service Info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilunsolicitedssinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwNotificationCode;           // @field indicates type of notification
    DWORD dwCallUserGroupIndex;         // @field indicates the CUG Index
    RILADDRESS raAddress;               // @field call address
    RILSUBADDRESS rsaSubAddress;        // @field subaddress
} RILUNSOLICITEDSSINFO, *LPRILUNSOLICITEDSSINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSERVICEINFO | Connection service information
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilserviceinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    BOOL fSynchronous;                  // @field TRUE if connection service is synchronous, FALSE if asynchronous
    BOOL fTransparent;                  // @field TRUE if connection service is transparent, FALSE if non-transparent
} RILSERVICEINFO, *LPRILSERVICEINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILRINGINFO | Ring information
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilringinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwCallType;                   // @field type of the offered call (<def RIL_CALLTYPE_> constant)
    DWORD dwAddressId;                  // @field indicates address ID on which the incoming call arrived (if available)
    RILSERVICEINFO rsiServiceInfo;      // @field data connection service information (set only for <def RIL_CALLTYPE_DATA>)
} RILRINGINFO, *LPRILRINGINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILDIALINFO | Ring information
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rildialinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    HRESULT hrCmdId;                    // @field handle of call being dialed
    DWORD dwCallId;                     // @field id of call being dialed
} RILDIALINFO, *LPRILDIALINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCONNECTINFO | Connection info
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilconnectinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwCallType;                   // @field type of the established connection (<def RIL_CALLTYPE_> constant)
    DWORD dwBaudRate;                   // @field Baud rate of the established connection (set only for <def RIL_CALLTYPE_DATA>)
} RILCONNECTINFO, *LPRILCONNECTINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILMSGSTORAGEINFO | Message storage locations
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilmsgstorageinfo_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwReadLocation;               // @field current read location
    DWORD dwWriteLocation;              // @field current write location
    DWORD dwStoreLocation;              // @field current store location
} RILMSGSTORAGEINFO, *LPRILMSGSTORAGEINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILSUPSERVICEDATA | Supplementary service data
//
// @comm None
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilsupservicedata_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwStatus;                     // @field additional status for message
    BYTE pbData[];                      // @field message itself
} RILSUPSERVICEDATA, *LPRILSUPSERVICEDATA;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSDIAL | Dialing capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapsdial_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwCallTypes;                  // @field type of call being placed
    DWORD dwOptions;                    // @field dialing options
} RILCAPSDIAL, *LPRILCAPSDIAL;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSBEARERSVC | Bearer service capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapsbearersvc_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwSpeeds1;                    // @field TBD
    DWORD dwSpeeds2;                    // @field TBD
    DWORD dwServiceNames;               // @field TBD
    DWORD dwConnectionElements;         // @field TBD
} RILCAPSBEARERSVC, *LPRILCAPSBEARERSVC;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSRLP | Radio Link Protocol capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapsrlp_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwVersion;                    // @field TBD
    RILRANGE rrIWSRange;                // @field TBD
    RILRANGE rrMWSRange;                // @field TBD
    RILRANGE rrAckTimerRange;           // @field TBD
    RILRANGE rrRetransmissionAttsRange; // @field TBD
    RILRANGE rrReseqPeriodRange;        // @field TBD
} RILCAPSRLP, *LPRILCAPSRLP;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSMSGMEMORYLOCATIONS | Message memory location capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapsmsgmemorylocations_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwReadLocations;              // @field supported read locations
    DWORD dwWriteLocations;             // @field supported write locations
    DWORD dwStoreLocations;             // @field supported store locations
} RILCAPSMSGMEMORYLOCATIONS, *LPRILCAPSMSGMEMORYLOCATIONS;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSLOCKINGPWDLENGTH | Locking password length capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapslockingpwdlength_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwFacility;                   // @field the locking facility
    DWORD dwPasswordLength;             // @field maximum password length
} RILCAPSLOCKINGPWDLENGTH, *LPRILCAPSLOCKINGPWDLENGTH;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSBARRINGPWDLENGTH | Call barring password length capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapsbarringpwdlength_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwType;                       // @field type of call barring
    DWORD dwPasswordLength;             // @field maximum password length
} RILCAPSBARRINGPWDLENGTH, *LPRILCAPSBARRINGPWDLENGTH;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSDATACOMPRESSION | Data compression capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapsdatacompression_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwDirection;                  // @field indicates supported direction values
    DWORD dwNegotiation;                // @field indicates supported negotiation values
    RILRANGE rrMaxDict;                 // @field range of supported max_dict values
    RILRANGE rrMaxString;               // @field range of supported max_string values
} RILCAPSDATACOMPRESSION, *LPRILCAPSDATACOMPRESSION;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILERRORCORRECTIONINFO | Error correction settings
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapserrorcorrection_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwOriginalRequest;            // @field indicates supported originator request values
    DWORD dwOriginalFallback;           // @field indicates supported originator fallback values
    DWORD dwAnswererFallback;           // @field indicates supported answerer fallback values
} RILCAPSERRORCORRECTION, *LPRILCAPSERRORCORRECTION;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSHSCSD | High Speed Circuit Switched Data capabilities
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapshscsd_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwMultislotClass;             // @field multislot class supported
    DWORD dwMaxRxTimeslots;             // @field maximum number of receive timeslots
    DWORD dwMaxTxTimeslots;             // @field maximum number of transmit timeslots
    DWORD dwMaxTotalTimeslots;          // @field maximum number of total timeslots
    DWORD dwChannelCodings;             // @field supported channel codings
    DWORD dwAirInterfaceUserRates;      // @field supported air interfacerates
    RILRANGE rrTopRxTimeslotRange;      // @field TBD
} RILCAPSHSCSD, *LPRILCAPSHSCSD;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILCAPSPBENTRYLENGTH | Phone book entry length maximum values
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilcapspbentrylength_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwMaxAddressLength;           // @field maximum length of the phone number portion
    DWORD dwMaxTextLength;              // @field maximum length in characters of the text portion
} RILCAPSPBENTRYLENGTH, *LPRILCAPSPBENTRYLENGTH;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSPROTOCOLCAPS | General Packet Radio Service capabilities
//
// @comm TBDTBD
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilgprscontextcaps_tag {
    DWORD cbSize;                       // @field structure size in bytes (padded to DWORD)
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwProtocolType;               // @field a RIL_GPRSPROTOCOL_* constant
    RILRANGE ContextIDRange;                 // @field min/max context ids
    DWORD dwDataCompression;            // @field valid data compression values
    DWORD dwHeaderCompression;          // @field valid header compression values
    DWORD dwParameterLength;           // @field length of parameters list in bytes
    char  szParameters[];               // @field valid string parameters of this prococol type, delimited by \0, with final param terminated by \0\0
} RILGPRSCONTEXTCAPS, *LPRILGPRSCONTEXTCAPS;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSCONTEXT | A PDP Context represents a certain configuration for
//         packet data communication.
//
// @comm None
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilgprscontext_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwContextID;                  // @field the context number
    DWORD dwProtocolType;               // @field a RIL_GPRSPROTOCOL_*constant
    WCHAR wszAccessPointName[MAXLENGTH_GPRSACCESSPOINTNAME];
                                        // @field a logical name to select the gateway gprs
                                        //        (which defines the external packet data network to use)
    WCHAR wszAddress[MAXLENGTH_GPRSADDRESS]; // @field the packet address to use (if null, request dynamic)
    DWORD dwDataCompression;             // @field a RIL_GPRSDATACOMP_*
    DWORD dwHeaderCompression;           // @field a RIL_GPRSHEADERCOMP_*
    DWORD dwParameterLength;            // @field length of parameters list
    char szParameters[];              // @field parameters specific to the prococol type
} RILGPRSCONTEXT, *LPRILGPRSCONTEXT;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSPROTOCOLCAPS | General Packet Radio Service capabilities
//
// @comm TBDTBD
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilgprsqosprofilecaps_tag {
    DWORD cbSize;                       // @field structure size in bytes (padded to DWORD)
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwProtocolType;               // @field a RIL_GPRSPROTOCOL_* constant
    DWORD dwPrecedenceClass;            // @field valid RIL_GPRSPRECEDENCECLASS_* constants
    DWORD dwDelayClass;                 // @field valid RIL_GPRSDELAYCLASS_* constants
    DWORD dwReliabilityClass;           // @field valid RIL_GPRSRELIABILITYCLASS_* constants
    DWORD dwPeakThruClass;              // @field valid RIL_GPRSPEAKTHRUCLASS_* constants
    DWORD dwMeanThruClass;              // @field valid RIL_GPRSMEANTHRUCLASS_* constants
} RILGPRSQOSPROFILECAPS, *LPRILGPRSQOSPROFILECAPS;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSPROTOCOLCAPS | General Packet Radio Service capabilities
//
// @comm TBDTBD
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilgprspacketsupportcaps_tag
{
    DWORD   cbSize;                     // @field structure size in bytes
    DWORD   dwParams;                   // @field indicates valid parameters
    BOOL    fPacket;                    // @parm TRUE: supports packet IO, FALSE: supports PPP/RAS connection only
    DWORD   dwActiveContexts;           // @parm number of simultaneous open connections
} RILGPRSPACKETSUPPORTCAPS, *LPRILGPRSPACKETSUPPORTCAPS;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSQOSPROFILE | A quality of service profile
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilgprsqosprofile_tag {
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwParams;                     // @field indicates valid parameters
    DWORD dwContextID;                  // @field the context number
    DWORD dwPrecedenceClass;            // @field a RIL_GPRSPRECEDENCECLASS_* constant
    DWORD dwDelayClass;                 // @field a RIL_GPRSDELAYCLASS_* constant
    DWORD dwReliabilityClass;           // @field a RIL_GPRSRELIABILITYCLASS_* constant
    DWORD dwPeakThruClass;              // @field a RIL_GPRSPEAKTHRUCLASS_* constant
    DWORD dwMeanThruClass;              // @field a RIL_GPRSMEANTHRUCLASS_* constant
} RILGPRSQOSPROFILE, *LPRILGPRSQOSPROFILE;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSANSWER | A quality of service profile
//
// @comm None
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilgprsanswer_tag
{
    DWORD cbSize;                       // @field structure size in bytes
    BOOL fAnswer;                       // @parm TRUE: accept, FALSE: reject
    DWORD dwL2Protocol;                 // @parm an optional RILL2PROTOCOL_* constant
    DWORD dwNumContexts;                // @parm number of contexts which follow
    DWORD dwContextID[];               // @parm identifies the context(s) to enter data state
} RILGPRSANSWER, *LPRILGPRSANSWER;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILENTERGPRSDATAMODE | A quality of service profile
//
// @comm None
//
// -----------------------------------------------------------------------------
#pragma warning(disable : 4200) // Disable "C4200: nonstandard extension used : zero-sized array in struct/union"
typedef struct rilentergprsdatamode_tag
{
    DWORD cbSize;                       // @field structure size in bytes
    DWORD dwL2Protocol;                 // @parm an optional RILL2PROTOCOL_* constant
    DWORD dwNumContexts;                // @parm number of contexts which follow
    DWORD dwContextID[];               // @parm identifies the context(s) to enter data state
} RILENTERGPRSDATAMODE, *LPRILENTERGPRSDATAMODE;
#pragma warning(default : 4200)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILGPRSCONTEXTACTIVATED | Shows which contexts are active
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilgprscontextactivated_tag
{
    DWORD cbSize;           // @field structure size in bytes
    DWORD dwContextID;      // @field the context number
    BOOL fActivated;        // @field whether the context is activated
} RILGPRSCONTEXTACTIVATED, *LPRILGPRSCONTEXTACTIVATED;



// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILLOGATCOMMAND | Contains inbound and outbound AT commands/responses
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rillogatinfo_tag
{
    DWORD cbSize;                // @field structure size in bytes
    DWORD cbLength;              // @field command buffer length
    BYTE szRsp[MAXLENGTH_CMD];   // @field command buffer
} RILLOGATINFO, *LPRILLOGATINFO;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILNDISIPV6ADDR | Encapsulates an IPv6 address.
//
// @comm None
//
// -----------------------------------------------------------------------------
#define IPV6_ADDRESS_LENGTH             16
#define IPV6_ADDRESS_LENGTH_IN_UCHAR    IPV6_ADDRESS_LENGTH
#define IPV6_ADDRESS_LENGTH_IN_USHORT   (IPV6_ADDRESS_LENGTH/2)
typedef struct rilndisipv6addr_tag
{
    union
    {
        UCHAR  Byte[IPV6_ADDRESS_LENGTH_IN_UCHAR];
        USHORT Word[IPV6_ADDRESS_LENGTH_IN_USHORT];
    };
} RILNDISIPV6ADDR, *LPRILNDISIPV6ADDR;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILNDISIPCONFIG | returned in association with  RIL_NOTIFY_NDIS_IPCONFIG
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilndisipconfig_tag
{
    DWORD  cbSize;                    // @field structure size in bytes
    DWORD  dwContextId;
    DWORD  dwProtocol;                // @field discriminator for the union field; defined by RIL_PARAM_NDISIPCONFIG_PROTOCOL_*
    union
    {
        struct
        {
            DWORD  dwFlags;           // @field bitfield of valid in_addr parameters defined by RIL_PARAM_NDISIPCONFIG_xxx
            DWORD  inIPAddress;
            DWORD  inPrimaryDNS;
            DWORD  inSecondaryDNS;
            DWORD  inDefaultGateway;
            DWORD  inSubnetMask;
        } ipv4;
        struct
        {
            DWORD  dwFlags;           // @field bitfield of valid in_addr parameters defined by RIL_PARAM_NDISIPCONFIG_IPV6_xxx
            RILNDISIPV6ADDR  inIPAddress;
            RILNDISIPV6ADDR  inPrimaryDNS;
            RILNDISIPV6ADDR  inSecondaryDNS;
            RILNDISIPV6ADDR  inDefaultGateway;
            RILNDISIPV6ADDR  inSubnetMask;
            DWORD  dwFlowInfo;
            DWORD  dwScopeId;
        } ipv6;
    };
} RILNDISIPCONFIG, *LPRILNDISIPCONFIG;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILNDISBUFFER | Buffer defintion for use in rildndispacket_tag below.
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilndisbuffer_tag
{
    BYTE *pbyBytes;                            // @field Pointer to the buffer
    DWORD cByteCount;                            // @field Number of bytes pointed to by pbyBytes.
} RILNDISBUFFER, *LPRILNDISBUFFER;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILNDISPACKET |
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilndispacket_tag
{
    DWORD dwContextId;
    DWORD dwSize;
    DWORD cBufferCount;
    RILNDISBUFFER NDISBuffer[1];
} RILNDISPACKET, *LPRILNDISPACKET;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILNDISGPRSCONTEXT |
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilndisgprscontext_tag
{
    DWORD         cbSize;             // @field structure size in bytes
    DWORD         dwParams;           // @field indicates valid parameters
    DWORD         dwContextID;        // @field identifies the context
    BOOL          fContextActivation; // @field TRUE: activated, FALSE: deactivated
    DWORD         userNameLength;     // @field login name
    const TCHAR  *username_p;         // @field name size
    DWORD         passwordLength;     // @field login password
    const TCHAR  *password_p;         // @field  password length
} RILNDISGPRSCONTEXT, *LPRILNDISGPRSCONTEXT;

typedef  RILNDISGPRSCONTEXT  RILNDISSETGPRSCONTEXTACTIVATED;
typedef  LPRILNDISGPRSCONTEXT LPRILNDISSETGPRSCONTEXTACTIVATED;

//
// RIL handle type
//
typedef HANDLE HRIL, *LPHRIL;


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func RIL function result callback
//
// @comm This function is called to send a return value after and asynchronous
//       RIL function call
//
// -----------------------------------------------------------------------------
typedef void (CALLBACK *RILRESULTCALLBACK)(
    DWORD       dwCode,     // @parm result code
    HRESULT     hrCmdID,    // @parm ID returned by the command that originated this response
    const void* lpData,     // @parm data associated with the notification
    DWORD       cbData,     // @parm size of the strcuture pointed to lpData
    DWORD       dwParam     // @parm parameter passed to <f RIL_Initialize>
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func RIL notification callback
//
// @comm This function is called when the radio sends an unsolicited notifiation
//
// -----------------------------------------------------------------------------
typedef void (CALLBACK *RILNOTIFYCALLBACK)(
    DWORD dwCode,           // @parm notification code
    const void* lpData,     // @parm data associated with the notification
    DWORD cbData,           // @parm size of the strcuture pointed to lpData
    DWORD dwParam           // @parm parameter passed to <f RIL_Initialize>
);

//
// RIL Functions
//

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Initializes RIL for use by this client
//
// @comm Synchronous
//      RIL only supports single threaded RIL handles.
//      The RIL validates the application's RIL handle before using it.
//              No application can use/close a RIL handle that it does not own.
//
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
typedef HRESULT (*RIL_InitializeProc)
//HRESULT RIL_Initialize
(
    DWORD dwIndex,                      // @parm index of the RIL port to use (e.g., 1 for RIL1:)
    RILRESULTCALLBACK pfnResult,        // @parm function result callback
    RILNOTIFYCALLBACK pfnNotify,        // @parm notification callback
    DWORD dwNotificationClasses,        // @parm classes of notifications to be enabled for this client
    DWORD dwParam,                      // @parm custom parameter passed to result and notififcation callbacks
    HRIL* lphRil                        // @parm returned handle to RIL instance
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Initializes RIL for use by this emergency call module
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
HRESULT RIL_InitializeEmergency(
    DWORD dwIndex,                      // @parm index of the RIL port to use (e.g., 1 for RIL1:)
    RILRESULTCALLBACK pfnResult,        // @parm function result callback
    RILNOTIFYCALLBACK pfnNotify,        // @parm notification callback
    DWORD dwNotificationClasses,        // @parm classes of notifications to be enabled for this client
    DWORD dwParam,                      // @parm custom parameter passed to result and notififcation callbacks
    HRIL* lphRil                        // @parm returned handle to RIL instance
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Deinitializes RIL
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
typedef HRESULT (*RIL_DeinitializeProc)
//HRESULT RIL_Deinitialize
(
    HRIL hRil                           // @parm handle to an RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enables additional classes of notifications for this client
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------

HRESULT RIL_EnableNotifications(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwNotificationClasses         // @parm classes of notifications to enable
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Disables classes of notifications for this client
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
HRESULT RIL_DisableNotifications(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwNotificationClasses         // @parm classes of notifications to disable
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Disables classes of notifications for this client
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
HRESULT RIL_RegisterATCommandLogging(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    BOOL fEnable         // @parm flag to turn feature on or off.
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Creates a log file of recent AT commands
//
// @comm Asynchronous.
//       For Microsoft Test only. This will not be fully implemented on every
//       platform. A return response of E_NOTIMPL will be returned in the
//       default case.
//
//       DO NOT IMPLEMENT THIS.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ATCommandLogFile(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPTSTR pszFilename                  // @parm String containing the filename for the log.
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves a serial port handle to be used for data communications
//
// @comm Synchronous.  Client is responsible for closing the handle returned in <p lphSerial>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSerialPortHandle(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    HANDLE* lphSerial                   // @parm pointer to the serial port handle
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves a serial port handle to be used for data communications
//
// @comm Synchronous.  Client is responsible for closing the handle returned in <p lphSerial>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSerialPortHandleFromContextID(
    HRIL hRil,                         // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwContextID,                 // @parm PDP context identifier.
    HANDLE *lphSerial                  // @parm pointer to the serial port handle
);
// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves a serial port handle statistics
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSerialPortStatistics(
    HRIL hRil,                              // @parm handle to RIL instance returned by <f RIL_Initialize>
    RILSERIALPORTSTATS* lpSerialPortStats   // @parm pointer to the statistics structure
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Restrieves the driver version
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetDriverVersion(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD *pdwVersion                   // @parm pointer to version.  HIWORD is major version, LOWORD is minor version
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Restrieves information about subscriber numbers
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILSUBSCRIBERINFO> structures.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSubscriberNumbers(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the list of available operators
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILOPERATORINFO> structures.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetOperatorList(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the built-in list of all known operators.
//       This is not the list of operators available, for that see RIL_GetOperatorList.
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILOPERATORNAMES> structures.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetAllOperatorsList(
    HRIL hRil                          // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the list of preferred operators
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILOPERATORINFO> structures.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetPreferredOperatorList(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwFormat                      // @parm format to use for the operator names in the list
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Adds a specified operator to the list of preferred operators
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_AddPreferredOperator(
    HRIL hRil,                              // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwIndex,                          // @parm storage index to use for the added operator
    const RILOPERATORNAMES* lpOperatorNames // @parm operator name
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Removes a specified operator from the list of preferred operators
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_RemovePreferredOperator(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwIndex                       // @parm storage index of the preferred operator to remove
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the operator the ME is currently registered with
//
// @comm Asynchronous.  <p lpData> points to an <t RILOPERATORNAMES> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCurrentOperator(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize()>
    DWORD dwFormat                      // @parm format of the operator name to return (<def RIL_OPFORMAT_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Registers the ME with a network operator
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_RegisterOnNetwork(
    HRIL hRil,                              // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwMode,                           // @parm operator selection mode (<def RIL_OPSELMODE_> constant)
    const RILOPERATORNAMES* lpOperatorNames // @parm operator to be selected (can be <def NULL> if <p dwMode> is <def RIL_OPSELMODE_AUTOMATIC>)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Unregisters the ME from the current newtwork operator
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_UnregisterFromNetwork(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current phone registration status
//
// @comm Asynchronous.  <p lpData> points to a <def RIL_REGSTAT_> constant.
//
// -----------------------------------------------------------------------------
typedef HRESULT (*RIL_GetRegistrationStatusProc)
//HRESULT RIL_GetRegistrationStatus
(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current CallerID settings
//
// @comm Asynchronous.  <p lpData> points to an <t RILCALLERIDSETTINGS> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCallerIdSettings(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the current CallerID status
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCallerIdStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwStatus                      // @parm status to be set (<def RIL_SVCSTAT_> constant)
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current HideID settings
//
// @comm Asynchronous.  <p lpData> points to an <t RILHIDEIDSETTINGS> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetHideIdSettings(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enables or disables HideID service
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetHideIdStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwStatus                      // @parm status to be set (<def RIL_SVCSTAT_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current DialedID settings
//
// @comm Asynchronous.  <p lpData> points to an <t RILDIALEDIDSETTINGS> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetDialedIdSettings(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the current DialedID settings
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetDialedIdStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwStatus                      // @parm status to be set (<def RIL_SVCSTAT_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current HideConnectedID settings
//
// @comm Asynchronous.  <p lpData> points to an <t RILHIDECONNECTEDIDSETTINGS> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetHideConnectedIdSettings(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the current HideConnectedID settings
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetHideConnectedIdStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwStatus                      // @parm status to be set (<def RIL_SVCSTAT_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the status for a Completion of Call to Busy Subscriber index.
//
// @comm Asynchronous.  If active, <p lpData> points to an array of <t char>s
//       indicating the phone number for which CCBS is active.  If CCBS is not
//       active for that entry, <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCCBSStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwCCBSIndex                   // @parm indicates which entry to query
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Clears registration for a Completion of Call to Busy Subscriber index.
//       Activation of CCBS is used by calling RIL_ManageCalls using the
//       <def RIL_CALLCMD_INVOKECCBS> flag.
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ClearCCBSRegistration(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwCCBSIndex                   // @parm indicates which entry to clear, may be <def RIL_CCBS_ALL>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current Closed User Group settings
//
// @comm Asynchronous.  <p lpData> points to an <t RILCLOSEDGROUPSETTINGS> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetClosedGroupSettings(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the Closed User Group settings
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetClosedGroupSettings(
    HRIL hRil,                                  // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILCLOSEDGROUPSETTINGS* lpSettings    // @parm settings to be set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves current Call Forwarding rules
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILCALLFORWARDINGSETTINGS> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCallForwardingSettings(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwReason,                     // @parm forwarding reason to retrieve the settings for (<def RIL_FWDREASON_> constant)
    DWORD dwInfoClass                   // @parm information class to retrieve barring status for (<def RIL_INFOCLASS_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Adds a Call Forwarding rule
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_AddCallForwarding(
    HRIL hRil,                                  // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwReason,                             // @parm forwarding reason to add Call Forwarding for (<def RIL_FWDREASON_> constant)
    const RILCALLFORWARDINGSETTINGS* lpSettings // @parm settings for the new Call Forwarding rule
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Removes a Call Forwarding rule
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_RemoveCallForwarding(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwReason,                     // @parm forwarding reason to remove Call Forwarding for (<def RIL_FWDREASON_> constant)
    DWORD dwInfoClasses                 // @parm information classes to remove Call Forwarding for (combination of
                                        //     <def RIL_INFOCLASS_> constants)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enables or disables the specified Call Forwarding rule
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCallForwardingStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwReason,                     // @parm forwarding reason to enable/disable Call Forwarding for (<def RIL_FWDREASON_> constant)
    DWORD dwInfoClasses,                // @parm information classes to enable/disable Call Forwarding for (combination of
                                        //     <def RIL_INFOCLASS_> constants)
    DWORD dwStatus                      // @parm status to be set (<def RIL_SVCSTAT_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves info classes that Call Waiting is currently enabled for
//
// @comm Asynchronous.  <p lpData> points to DWORD containing a combination
//       of <def RIL_INFOCLASS_> constants.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCallWaitingSettings(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwInfoClass                   // @parm information class to retrieve barring status for (<def RIL_INFOCLASS_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enables or disables Call Waiting for the specified info class
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCallWaitingStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwInfoClasses,                // @parm information classes to enable/disable Call Waiting for
    DWORD dwStatus                      // @parm status to be set (<def RIL_SVCSTAT_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends supplementary service (USSD) data
//
// @comm TBD
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendSupServiceData(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const BYTE* lpbData,                // @parm data to be sent
    DWORD dwSize                        // @parm size of the data pointed to by <p lpbData> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Cancels current supplementary service session
//
// @comm TBD
//
// -----------------------------------------------------------------------------
HRESULT RIL_CancelSupServiceDataSession(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current address identifier (see RILSUBSCRIBERINFO)
//
// @comm Asynchronous.  <p lpData> points to a <def DWORD> identifying the current address ID.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCurrentAddressId(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the current address identifier (see RILSUBSCRIBERINFO)
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCurrentAddressId(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwAddressId                   // @parm identifies the new addressID to use
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Dials a specified address
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_Dial(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPCSTR lpszAddress,                 // @parm address to dial (no longer than <def MAXLENGTH_ADDRESS> chars)
    DWORD dwType,                       // @parm type of the call to establish (<def RIL_CALLTYPE_> constant)
    DWORD dwOptions                     // @parm dialing options (any combination of <def RIL_DIALOPT_> constants)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Answers an incoming call
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_Answer(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Hangs up all calls currently in progress
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_Hangup(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends DTMF tones across an established voice call
//
// @comm Asynchronous.  <p lpData> is <def NULL>.  Function does not return until
//       DTMF tone has completed.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendDTMF(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPCSTR lpszChars,                   // @parm alphanumeric string representing DTMF tones to be sent (0-9, A-D, *, #)
    DWORD dwDuration                    // @parm new DTMF tone duration in milliseconds (<def RIL_DTMFDURATION_DEFAULT>
                                        // corresponds to the manufacturer's default value)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Detects DTMF tones from an established voice call
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetDTMFMonitoring(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    BOOL fEnable                        // @parm TRUE to initiate DTMF monitoring; FALSE to cancel
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the list of active, held, and waiting calls
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILCALLINFO> structures.
//       GSM call lists include information about zero to many calls, but CDMA
//       call lists consist of information about at most one call.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCallList(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Modifies the state of active, held, and waiting calls
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ManageCalls(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwCommand,                    // @parm call modification command to be performed (<def RIL_CALLCMD_> constant)
    DWORD dwID                          // @parm ID of the call to be modified (only for <def RIL_CALLCMD_RELEASECALL>
                                        //       and <def RIL_CALLCMD_HOLDALLBUTONE>)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Transfers incoming allerting call to the specified number
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_TransferCall(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILADDRESS* lpAddress,        // @parm address to transfer the call to
    const RILSUBADDRESS* lpSubAddress   // @parm sub-address to transfer the call to (can be <def NULL>)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the phone line status
//
// @comm Asynchronous.  <p lpData> points to <t DWORD> containing <def RIL_LINESTAT_> constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetLineStatus(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves audio gain information
//
// @comm Asynchronous.  <p lpData> points to an <t RILGAININFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetAudioGain(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets audio gain information
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetAudioGain(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILGAININFO* lpGainInfo       // @parm audio gain information to be sent
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves currently used transmit and receive audio devices
//
// @comm Asynchronous.  <p lpData> points to an <t RILAUDIODEVICEINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetAudioDevices(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets currently used transmit and receive audio devices
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetAudioDevices(
    HRIL hRil,                                  // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILAUDIODEVICEINFO* lpAudioDeviceInfo // @parm audio devices to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Determines whether the input audio device is muted
//
// @comm Asynchronous.  <p lpData> points to a <t BOOL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetAudioMuting(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Mutes or un-mutes the input audio device
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetAudioMuting(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    BOOL fEnable                        // @parm TRUE if input audio device is to be muted; FALSE otherwise
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves High Speeed Circuit Switched Data options
//
// @comm Asynchronous.  <p lpData> points to an <t RILHSCSDINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetHSCSDOptions(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets High Speeed Circuit Switched Data options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetHSCSDOptions(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILHSCSDINFO* lpHscsdInfo     // @parm High Speeed Circuit Switched Data options to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves High Speeed Circuit Switched Data options
//
// @comm Asynchronous.  <p lpData> points to an <t RILCALLHSCSDINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetHSCSDCallSettings(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves data compression options
//
// @comm Asynchronous.  <p lpData> points to an <t RILDATACOMPINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetDataCompression(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets data compression options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetDataCompression(
    HRIL hRil,                              // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILDATACOMPINFO* lpDataCompInfo   // @parm data compression options to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves error correction options
//
// @comm Asynchronous.  <p lpData> points to an <t RILERRORCORRECTIONINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetErrorCorrection(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Set error correction options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetErrorCorrection(
    HRIL hRil,                                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILERRORCORRECTIONINFO* lpErrorCorrectionInfo // @parm error correction options to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves currently set data bearer service options
//
// @comm Asynchronous.  <p lpData> points to an <t RILBEARERSVCINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetBearerServiceOptions(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets data bearer service options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetBearerServiceOptions(
    HRIL hRil,                                  // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILBEARERSVCINFO* lpBearerServiceInfo // @parm data bearer service options to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves currently set Radio Link Protocol options
//
// @comm Asynchronous.  <p lpData> points to an <t RILRLPINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetRLPOptions(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets Radio Link Protocol options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetRLPOptions(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILRLPINFO* lpRlpInfo         // @parm Radio Link Protocol options to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets currently set messaging service options
//
// @comm Asynchronous.  <p lpData> points to an <t RILMSGSERVICEINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetMsgServiceOptions(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets messaging service options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetMsgServiceOptions(
    HRIL hRil,                                  // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILMSGSERVICEINFO* lpMsgServiceInfo   // @parm messaging service options to be set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets currently set messaging configuration
//
// @comm Asynchronous.  <p lpData> points to an <t RILMSGCONFIG> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetMsgConfig(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets messaging configuration
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetMsgConfig(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILMSGCONFIG* lpMsgConfigInfo // @parm messaging configuration to be set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets cell broadcast messaging configuration
//
// @comm Asynchronous.  <p lpData> points to an <t RILCBMSGCONFIG> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCellBroadcastMsgConfig(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets cell broadcast messaging configuration
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCellBroadcastMsgConfig(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILCBMSGCONFIG* lpCbMsgConfigInfo // @parm messaging configuration to be set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Reads a message from the current storage location
//
// @comm Asynchronous.  <p lpData> points to an <t RILMESSAGEINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ReadMsg(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwIndex                       // @parm index of the message to be read
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Deletes a message from the current storage location
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_DeleteMsg(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwIndex                       // @parm index of the message to be deleted
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Writes a message to the current storage location
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> containing the index used.
//
// -----------------------------------------------------------------------------
HRESULT RIL_WriteMsg(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILMESSAGE* lpMessage,        // @parm message to be written (of type <def RIL_MSGTYPE_IN_DELIVER> or <def RIL_MSGTYPE_OUT_SUBMIT>)
    DWORD dwStatus                      // @parm status to assigned to the written message (<def RIL_MSGSTATUS_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a message
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> containing the reference
//       number of the sent message.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendMsg(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILMESSAGE* lpMessage,        // @parm message to be sent
    DWORD dwOptions                     // @parm options (any combination of <def RIL_SENDOPT_> constants)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a message from the current storage location
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> containing the reference
//       number of the sent message.  This feature is not used and is untested.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendStoredMsg(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwIndex,                      // @parm index of the message to be sent
    DWORD dwOptions                     // @parm options (any combination of <def RIL_SENDOPT_> constants)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends an message ackknowledgement
//
// @comm Asynchronous.  <p lpData> is <def NULL>.  On Phase 2 mobiles, the radio
//       automatically sends SMS message ACKs.  But in Phase 2+, the MMI is
//       responsible for these ACKs, hense this function.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendMsgAcknowledgement(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    BOOL fSuccess                       // @parm TRUE if success acknowledgment is to be sent; FALSE otherwise
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves International Mobile Subscriber Identity of the phone user
//
// @comm Asynchronous.  <p lpData> points to an array of <t char>s
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetUserIdentity(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves current locked state of the phone
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> containing a <def RIL_LOCKEDSTATE_> constant
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetPhoneLockedState(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Removes current lock applied to the phone
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_UnlockPhone(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPCSTR lpszPassword,                // @parm password to unlock the phone (no longer than <def MAXLENGTH_PASSWORD> chars)
    LPCSTR lpszNewPassword              // @parm new password (can be <def NULL>, unless the current locked state is
                                        //     one of the <def RIL_LOCKEDSTATE_*_PUK> constants; no longer than
                                        //     <def MAXLENGTH_PASSWORD> chars)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves locking status for the specified facility
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> containing a <def RIL_LOCKINGSTATUS_> constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetLockingStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwFacility,                   // @parm facility to retrieve locking status for (<def RIL_LOCKFACILITY_> constant)
    LPCSTR lpszPassword                 // @parm password to retrieve locking status (can be <def NULL> if password isn't required;
                                        //     no longer than MAXLENGTH_PASSWORD chars)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enables or disables locking status for the specified facility
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetLockingStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwFacility,                   // @parm facility to enable/disable locking for (<def RIL_LOCKFACILITY_> constant)
    LPCSTR lpszPassword,                // @parm password to enable/disable locking (can be <def NULL> if password isn't required;
                                        //     no longer than <def MAXLENGTH_PASSWORD> chars)
    DWORD dwStatus                      // @parm status to be set (<def RIL_LOCKINGSTATUS_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Changes locking password for the specified facility
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ChangeLockingPassword(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwFacility,                   // @parm facility to change locking password for (<def RIL_LOCKFACILITY_> constant)
    DWORD dwOldPasswordType,            // @parm the type of OLD password (PIN or PUK) RIL_PASSWORDTYPE_* constant
    LPCSTR lpszOldPassword,             // @parm current locking password (no longer than <def MAXLENGTH_PASSWORD> chars)
    LPCSTR lpszNewPassword              // @parm new locking password (no longer than <def MAXLENGTH_PASSWOR> chars)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves status of the specified type of call barring
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> containing a <def RIL_BARRINGSTATUS> constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCallBarringStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwType,                       // @parm type of call barring to retrieve status for (<def RIL_BARRTYPE_> constant)
    DWORD dwInfoClass,                  // @parm information class to retrieve barring status for (<def RIL_INFOCLASS_> constant)
    LPCSTR lpszPassword                 // @parm password to retrieve barring status (can be <def NULL> if password isn't required;
                                        //     no longer than <def MAXLENGTH_PASSWORD> chars)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enables or disables the specified type of call barring
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCallBarringStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwType,                       // @parm type of call barring to enable/disable (<def RIL_BARRTYPE_> constant)
    DWORD dwInfoClass,                  // @parm information class to enable/disable call barring for (<def RIL_INFOCLASS_> constant)
    LPCSTR lpszPassword,                // @parm password to enable/disable call barring (can be <def NULL> if password isn't required;
                                        //     no longer than <def MAXLENGTH_PASSWORD> chars)
    DWORD dwStatus                      // @parm status to be set (<def RIL_BARRINGSTATUS_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Changes password for the specified type of call barring
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ChangeCallBarringPassword(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwType,                       // @parm type of call barring to retrieve status for (<def RIL_BARRTYPE_> constant)
    LPCSTR lpwszOldPassword,            // @parm current password (no longer than <def MAXLENGTH_PASSWORD> chars)
    LPCSTR lpwszNewPassword             // @parm new password (no longer than <def MAXLENGTH_PASSWORD> chars)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves manufacturer equipment information
//
// @comm Asynchronous.  <p lpData> points to an <t RILEQUIPMENTINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetEquipmentInfo(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves currently set equipment state
//
// @comm Asynchronous.  <p lpData> points to an <t RILEQUIPMENTSTATE> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetEquipmentState(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the equipment to the specified state
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetEquipmentState(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwEquipmentState              // @parm equipment state to set (<def RIL_EQSTATE_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Proxy API to determine if the Radio is present or Not (Is the RIL driver Loaded?)
//
// @comm Synchronous
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetRadioPresence(
    HRIL hRIL,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD* dwRadioPresence              // @parm pointer to a DWORD (ouput param contains values from RIL_RADIOPRESENCE_*)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves currently set phonebook options
//
// @comm Asynchronous.  <p lpData> points to an <t RILPHONEBOOKINFO> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetPhonebookOptions(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets phonebook options
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetPhonebookOptions(
    HRIL hRil,                              // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILPHONEBOOKINFO* lpPhonebookInfo // @parm phonebook options to set
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Reads phonebook entries from the specified range of indices of the current storage location
//
// @comm Asynchronous.  <p lpData> points to an array of <t RILPHONEBOOKENTRY> structures.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ReadPhonebookEntries(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwStartIndex,                 // @parm starting index of the range
    DWORD dwEndIndex                    // @parm ending index of the range
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Writes a phonebook entry to the current storage location
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_WritePhonebookEntry(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILPHONEBOOKENTRY* lpEntry    // @parm phonebook entry to write out
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Deletes a phonebook entry from the current storage location
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_DeletePhonebookEntry(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwIndex                       // @parm index of the entry to delete
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a specified command to the SIM
//
// @comm Asynchronous.  <p lpData> points to an array of <t BYTE>s.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendSimCmd(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const BYTE* lpbCommand,             // @parm command to be sent to the SIM
    DWORD dwSize                        // @parm size of the data pointed to by <p lpbCommand> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the SIM's answer to reset data.
//
// @comm Asynchronous.  <p lpData> points to an <t RILATRINFO> structure.
// This command is not standardized and may be specific to each radio
// implementation, if implemented at all.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetATR(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a specified restricted command to the SIM
//
// @comm Asynchronous.  <p lpData> points to an <t RILSIMRESPONSE> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendRestrictedSimCmd(
    HRIL hRil,                                  // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwCommand,                            // @parm restricted command to be sent to the SIM (<def RIL_SIMCMD_> constant)
    const RILSIMCMDPARAMETERS* lpParameters,    // @parm Parameters for the command to be sent (can be <def NULL> if parameters aren't required)
    const BYTE* lpbData,                        // @parm Data to be written to the SIM (can be <def NULL> if data isn't required)
    DWORD dwSize                                // @parm Size of the data pointed to by <p lpbData> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves SIM Record Status
//
// @comm Asynchronous.  <p lpData> points to RILSIMRECORDSTATUS
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSimRecordStatus(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwFileID                      // @parm address of the file to read
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves SIM Toolkit terminal profile
//
// @comm Asynchronous.  <p lpData> points to an array of <t BYTE>s.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSimToolkitProfile(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets SIM Toolkit terminal profile
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetSimToolkitProfile(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const BYTE* lpbProfile,             // @parm SIM Toolkit profile to be set
    DWORD dwSize                        // @parm size of the data pointed to by <p lpbProfile> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a SIM Toolkit envelope command
//
// @comm Asynchronous.  <p lpData> points to an array of <t BYTE>s containing a
//       response to the sent command.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendSimToolkitEnvelopeCmd(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const BYTE* lpbCommand,             // @parm SIM Toolkit envelope command to be sent
    DWORD dwSize                        // @parm size of the data pointed to by <p lpbCommand> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Fetches a SIM Toolkit command from the SIM
//
// @comm Asynchronous.  <p lpData> points to an array of <t BYTE>s containing a
//       fetched command.
//
// -----------------------------------------------------------------------------
HRESULT RIL_FetchSimToolkitCmd(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a response to an executed SIM Toolkit command
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendSimToolkitCmdResponse(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILSIMTOOLKITRSP* pRsp,       // @parm Command Response to be sent.
    const LPBYTE pDetails,              // @parm Detailed command response to be sent (can be <def NULL> if details aren't required)
    DWORD dwDetailSize                  // @parm size of the details pointed to by <p pDetails> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Terminates the SIM Toolkit session
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_TerminateSimToolkitSession(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwCause                       // @parm cause for session termination (<def RIL_SIMTKITTERMCAUSE_> constant)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends a requested Event to the SIM.
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendSimToolkitEventDownload(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const DWORD dwEvent,                // @parm Event to be sent.
    const LPBYTE pData,                 // @parm Detailed event info to be sent (can be <def NULL> if details aren't required)
    DWORD dwDataSize                    // @parm size of the details pointed to by <p pDetails> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves advice-of-charge settings
//
// @comm Asynchronous.  <p lpData> points to a <t RILCOSTINFO> structure.
//       This feature is not used and is untested.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCostInfo(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets advice-of-charge settings
//
// @comm Asynchronous.  <p lpData> points to a <t RILCOSTINFO> structure.
//       This feature is not used and is untested.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetCostInfo(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILCOSTINFO* lpCostInfo,      // @parm advice-of-charge settings to set
    LPCSTR lpszPassword                 // @parm password requred to set advice-of-charge settings
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves information about the received signal quality
//
// @comm Asynchronous.  <p lpData> points to a <t RILSIGNALQUALITY> structure.
//
// -----------------------------------------------------------------------------
typedef HRESULT (*RIL_GetSignalQualityProc)
//HRESULT RIL_GetSignalQuality
(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves information about the cell tower currently used by the phone
//
// @comm Asynchronous.  <p lpData> points to a <t RILCELLTOWERINFO> structure.
//
// -----------------------------------------------------------------------------
typedef HRESULT (*RIL_GetCellTowerInfoProc)
//HRESULT RIL_GetCellTowerInfo
(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Performs an implementation-specific operation
//
// @comm Asynchronous.  <p lpData> points to an array of <t BYTE>s.
//
// -----------------------------------------------------------------------------
HRESULT RIL_DevSpecific(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const BYTE* lpbParams,              // @parm parameters for the operation to be performed
    DWORD dwSize                        // @parm size of the data pointed to by <p lpParams> in bytes
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves specified device capabilities
//
// @comm Asynchronous. <nl><nl><p dwCapsType> (<def RIL_CAPSTYPE_>)           <p lpData><nl>
//                     <def *_DIAL>                   points to an <t RILCAPSDIAL> structure<nl>
//                     <def *_DTMFDURATIONRANGE>      points to an <t RILRANGE> structure (values in milliseconds)<nl>
//                     <def *_CALLMGTCMDS>            points to <t DWORD> containing a combination of <def RIL_CAPS_CALLCMD_> constants<nl>
//                     <def *_BEARERSERVICE>          points to an  <t RILCAPSBEARERSVC> structure<nl>
//                     <def *_RLP>                    points to an array of <t RILAPSRLP> structures<nl>
//                     <def *_EQUIPMENTSTATES>        points to <t DWORD> containing a combination of <def RIL_CAPS_EQSTATE_> constants<nl>
//                     <def *_PBSTORELOCATIONS>       points to <t DWORD> containing a combination of <def RIL_CAPS_PBLOC_> constants<nl>
//                     <def *_PBINDEXRANGE>           points to an <t RILRANGE> structure<nl>
//                     <def *_PBENTRYLENGTH>          points to an <t RILCAPSPBENTRYLENGTH> strcuture<nl>
//                     <def *_MSGSERVICETYPES>        points to <t DWORD> containing a combination of <def RIL_CAPS_MSGSVCTYPE_> constants<nl>
//                     <def *_MSGMEMORYLOCATIONS>     points to an <t RILCAPSMSGMEMORYLOCATIONS> structure<nl>
//                     <def *_BROADCASTMSGLANGS>      points to <t DWORD> containing a combination of <def RIL_CAPS_DCSLANG_> constants<nl>
//                     <def *_MSGCONFIGINDEXRANGE>    points to an <t RILRANGE> structure<nl>
//                     <def *_MSGSTATUSVALUES>        points to <t DWORD> containing a combination of <def RIL_CAPS_MSGSTATUS_> constants<nl>
//                     <def *_PREFOPINDEXRANGE>       points to an <t RILRANGE> structure<nl>
//                     <def *_LOCKFACILITIES>         points to <t DWORD> containing a combination of <def RIL_CAPS_LOCKFACILITY_> constants<nl>
//                     <def *_LOCKINGPWDLENGTHS>      points to an array of <t RILCAPSLOCKINGPWDLENGTH> structures<nl>
//                     <def *_BARRTYPES>              points to <t DWORD> containing a combination of <def RIL_CAPS_BARRTYPE_> constants<nl>
//                     <def *_BARRINGPWDLENGTHS>      points to an array of <t RILCAPSBARRINGPWDLENGTH> structures<nl>
//                     <def *_FORWARDINGREASONS>      points to <t DWORD> containing a combination of <def RIL_CAPS_FWDREASON_> constants<nl>
//                     <def *_SIMTOOLKITNOTIFICATIONS>points to a <t TBD> SIMTOOLKIT structure <nl>
//                     <def *_INFOCLASSES>            points to <t DWORD> containing a combination of <def RIL_CAPS_INFOCLASS_> constants<nl>
//                     <def *_HSCSD>                  points to an <t RILCAPSHSCSD> structure<nl>
//                     <def *_GPRS>                   points to an <t RILCAPSGPRS> structure<nl>
//                     <def *_GPRSPACKETSUPPORT>      points to an <t RILGPRSPACKETSUPPORT> structure<nl>

HRESULT RIL_GetDevCaps(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwCapsType                    // @parm type of caps class to retrieve
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the systemtime from the network
//
// @comm Asynchronous.  <p lpData> points to a <t SYSTEMTIME> structure (containing the UTC time).
//       This feature is currently not used and is untested.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetSystemTime(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves a list GPRS contexts
//
// @comm Asynchronous.  <p lpData> points to a <t RILGPRSCONTEXT> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetGPRSContextList(
    HRIL hRil                          // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets a particular GPRS context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetGPRSContext(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILGPRSCONTEXT* lpGprsContext // @parm points to a <t RILGPRSCONTEXT> structure
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Deletes a particular GPRS context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_DeleteGPRSContext(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwContextID                   // @parm identifies which context to delete
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the requested quality of service profile for all contexts
//
// @comm Asynchronous.  <p lpData> points to a <t RILGPRSQOSPROFILE> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetRequestedQualityOfServiceList(
    HRIL hRil                          // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the requested quality of service profile for a context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetRequestedQualityOfService(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILGPRSQOSPROFILE* lpGprsQosProfile // @parm points to a <t RILGPRSQOSPROFILE> structure
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Deletes the requested quality of service profile for a context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_DeleteRequestedQualityOfService(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwContextID                   // @parm identifies which profile to delete
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the minimum quality of service profile for all contexts
//
// @comm Asynchronous.  <p lpData> points to a <t RILGPRSQOSPROFILE> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetMinimumQualityOfServiceList(
    HRIL hRil                          // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the minimum quality of service profile for a context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetMinimumQualityOfService(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILGPRSQOSPROFILE* lpGprsQosProfile // @parm points to a <t RILGPRSQOSPROFILE> structure
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Deletes the minimum quality of service profile for a context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_DeleteMinimumQualityOfService(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwContextID                   // @parm identifies which profile to delete
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the GPRS attach state
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetGPRSAttached(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    BOOL fAttached                      // @parm TRUE: attached, FALSE: detached
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the GPRS attach state
//
// @comm Asynchronous.  <p lpData> points to a <t BOOL> indicating attach state.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetGPRSAttached(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the GPRS activation state for a context
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetGPRSContextActivated(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwContextID,                  // @parm identifies the context
    BOOL fContextActivation             // @parm TRUE: activated, FALSE: deactivated
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the GPRS activation state for all contexts
//
// @comm Asynchronous.  <p lpData> points to a <t RILGPRSCONTEXTACTIVATED> indicating activation state.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetGPRSContextActivatedList(
    HRIL hRil                          // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Enters into GPRS data state
//
// @comm Asynchronous.  <p lpData> if <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_EnterGPRSDataMode(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILENTERGPRSDATAMODE* lpEnterGprsDataMode // @parm points to a <t RILENTERGPRSDATAMODE> structure
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the PDP address for a particular context
//
// @comm Asynchronous.  <p lpData> points to an array of <t WCHAR> values indicating the address.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetGPRSAddress(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwContextID                   // @parm identifies the context
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Answers an incoming GPRS activation request
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GPRSAnswer(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILGPRSANSWER* lpGprsAnswer   // @parm points to a <t RILGPRSANSWER> structure
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current GPRS registration status
//
// @comm Asynchronous.  <p lpData> points to a <def RIL_REGSTAT_> constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetGPRSRegistrationStatus(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the current GPRS class
//
// @comm Asynchronous.  <p lpData> points to a <def RIL_GPRSCLASS_> constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetGPRSClass(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the current GPRS class
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetGPRSClass(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwClass                       // @parm a RIL_GPRSCLASS_* constant
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the preferred SMS service option for mobile originated messages
//
// @comm Asynchronous.  <p lpData> points to a <def RIL_MOSMSSERVICE_> constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetMOSMSService(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the preferred SMS service option for mobile originated messages
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetMOSMSService(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwMoSmsService                // @parm a RIL_MOSMSSERVICE_* constant
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @params RILBYTECOUNTER
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_PARAM_BC_RXBYTECOUNT                    (0x00000001) // @paramdefine
#define RIL_PARAM_BC_TXBYTECOUNT                    (0x00000002) // @paramdefine
#define RIL_PARAM_BC_TOTALBYTECOUNT                 (0x00000004) // @paramdefine
#define RIL_PARAM_BC_ALL                            (0x00000007) // @paramdefine

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @struct RILBYTECOUNTER | Represents the cumulative number of bytes transferred by the radio (packet).
//
// @comm None
//
// -----------------------------------------------------------------------------
typedef struct rilbytecounter_tag {
    DWORD cbSize;                           // @field structure size in bytes
    DWORD dwParams;                         // @field indicates valid parameters
    DWORD dwRxByte;                         // @field Number of received bytes
    DWORD dwTxByte;                         // @field Number of transmitted bytes
    DWORD dwTotalByte;                      // @field Total Number of bytes transferred (This comes from the radio, not RxByte+TxByte)
} RILBYTECOUNTER, *LPRILBYTECOUNTER;

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Retrieves the cumulative count of data bytes transferred by the radio (packet)
//
// @comm Asynchronous.  <p lpData> points to a <t RILBYTECOUNTER> structure.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetPacketByteCount(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Resets the cumulative count of data bytes transferred by the radio (packet) to zero.
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_ResetPacketByteCount(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants System Coverage | Current System Coverage
//
// @comm Various Levels of CDMA and GSM Coverage
//
// -----------------------------------------------------------------------------
#define RIL_SYSTEMTYPE_NONE                         (0x00000000)      // @constdegine No Networks in Coverage
#define RIL_SYSTEMTYPE_IS95A                        (0x00000001)      // @constdefine IS-95A network support (Low Packet, or Circuit Switched Service)
#define RIL_SYSTEMTYPE_IS95B                        (0x00000002)      // @constdefine IS-95B network support
#define RIL_SYSTEMTYPE_1XRTTPACKET                  (0x00000004)      // @constdefine CDMA-2000 Rev A (1xRTT) network support
#define RIL_SYSTEMTYPE_GSM                          (0x00000008)      // @constdefine GSM network support
#define RIL_SYSTEMTYPE_GPRS                         (0x00000010)      // @constdefine GPRS support
#define RIL_SYSTEMTYPE_EDGE                         (0x00000020)      // @constdefine GSM EDGE network support
#define RIL_SYSTEMTYPE_1XEVDOPACKET                 (0x00000040)      // @constdefine CDMA (1xEVDO) network support
#define RIL_SYSTEMTYPE_1XEVDVPACKET                 (0x00000080)      // @constdefine CDMA (1xEVDV) network support
#define RIL_SYSTEMTYPE_UMTS                         (0x00000100)      // @constdefine UMTS network support

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the Current System Coverage
//
// @comm Gets the Current type of System/Cellular connection that is available.
//       Asynchronous.  <p lpData> is <t DWORD> of type RIL_SYSTEMTYPE_ flags)
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCurrentSystemType(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Modifies the state of active, held, and waiting calls
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SendFlash(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPRILADDRESS lpraRilAddress         // @parm flash address
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CDMA Roaming Types | CDMA Roaming Types
//
// @comm The meaning of AUTOMATICA and AUTOMATICB is up to network specific interpretations
//
// -----------------------------------------------------------------------------
#define RIL_ROAMMODE_HOMEONLY           (0x00000001)            // @constdefine The User will never go off the home network
#define RIL_ROAMMODE_AUTOMATICA         (0x00000002)            // @constdefine Network define Roaming A (The effect of this setting is carrier dependent)
#define RIL_ROAMMODE_AUTOMATICB         (0x00000003)            // @constdefine Network define Roaming B (The effect of this setting is carrier dependent)

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the Roaming Mode in CDMA
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> of type RIL_ROAMMODE_*.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetRoamingMode(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Modifies the state of active, held, and waiting calls in CDMA and AMPS systems
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetRoamingMode(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwRoamingMode                 // @parm RIL_ROAMMODE_* constant
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CDMA Privacy Mode | CDMA Privacy Mode
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_CALLPRIVACY_STANDARD                       (0x00000001) // @constdefine Enhanced Call Privacy is OFF
#define RIL_CALLPRIVACY_ENHANCED                       (0x00000002) // @constdefine Enhanced Call Privacy is ON

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the user's preferred privacy settings
//
// @comm Asynchronous.  <p lpData> points to a RIL_CALLPRIVACY_* constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetPreferredPrivacyMode(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the user's preferred privacy settings
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_SetPreferredPrivacyMode(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    DWORD dwPreferredPrivacyMode        // @parm user's preferred privacy setting, uses RIL_CALLPRIVACY_* constant
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the privacy status of the current system
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> RIL_CALLPRIVACY_* constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCurrentPrivacyStatus(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sends the User string to the device for Akey verificaiton
//
// @comm Asynchronous. <p lpData> is <def NULL>. (Either it fails or succeeds)
// According to TSB-50 (up to 26 digits)
//
// -----------------------------------------------------------------------------

HRESULT RIL_SendAKey(
    HRIL hRil, // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPCSTR lpszChars // @parm numeric string representing akey digits (0-9, *, #)
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CDMA Location Serivces Status | Location Services
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_LOCATION_OFF                             0
#define RIL_LOCATION_ON                              1

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the current location status of the current system
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> RIL_LOCATION_* constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCurrentLocationStatus(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants CDMA Roaming Status | CDMA Roaming Status
//
// @comm None
//
// -----------------------------------------------------------------------------
#define RIL_ROAMSTATUS_NONE                          0
#define RIL_ROAMSTATUS_ANALOG                        1
#define RIL_ROAMSTATUS_DIGITAL                       2

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Gets the current roaming status of the current system
//
// @comm Asynchronous.  <p lpData> points to a <t DWORD> RIL_ROAMSTATUS_* constant.
//
// -----------------------------------------------------------------------------
HRESULT RIL_GetCurrentRoamingStatus(
    HRIL hRil                           // @parm handle to RIL instance returned by <f RIL_Initialize>
);

// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @constants SIM Refresh Constants | Constants which indicate which cached SIM regions to refresh.
//
// @comm The notification contains a DWORD value which contains the SIM record to refresh,
//       or one of the special values below
//
// -----------------------------------------------------------------------------
#define RIL_SIM_DATACHANGE_MSISDNS                        (0xffffffff)
#define RIL_SIM_DATACHANGE_ALL_SIMRECORDS                 (0xfffffffe)
#define RIL_SIM_DATACHANGE_ALL_SIMPB                      (0xfffffffd)
#define RIL_SIM_DATACHANGE_ALL                            (0xfffffffc)


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Hand off packet for lower level to send. Asynchronous result indicates buffer can be reused.
//
// @comm Asynchronous.  <p lpData> points to lpPacketToSend
//                      <cbData> is the size of lpPacketToSend
//
// -----------------------------------------------------------------------------

HRESULT RIL_NDIS_SendPacket(
    HRIL hRil, // @parm handle to RIL instance returned by <f RIL_Initialize>
    const LPRILNDISPACKET lpPacketToSend // @parm numeric string representing akey digits (0-9, *, #)
);


// -----------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Signals the lower levels that the packet is available for reuse.
//
// @comm Synchronous.
//
// -----------------------------------------------------------------------------

HRESULT RIL_NDIS_ReceivePacketDone(
    HRIL hRil, // @parm handle to RIL instance returned by <f RIL_Initialize>
    const LPRILNDISPACKET lpPacketReceived // @parm
);

// ---------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Sets the GPRS activation state for a context for an NDIS connection.
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_NDIS_SetGPRSContextActivated(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    const RILNDISGPRSCONTEXT *lpNdisSetGprsContextActivated // @parm
);


// ---------------------------------------------------------------------------
//
// @doc EXTERNAL
//
// @func Send an arbitrary string to the radio for logging purposes
//
// @comm Asynchronous.  <p lpData> is <def NULL>.
//
// -----------------------------------------------------------------------------
HRESULT RIL_LogEventToRadio(
    HRIL hRil,                          // @parm handle to RIL instance returned by <f RIL_Initialize>
    LPCSTR lpszChars	 // @parm
);



#ifdef __cplusplus
}
#endif


#endif // _RIL_H_
