#define Object NSObject*
#include "sipargs.h"
#include "mainview.h"

SipArgs SipArgsMake(int options, id control, bool secret, NSString *text)
{
    SipArgs args;
    args.options = options;
    args.control = control;
    args.secret = secret;
    args.text = text;
    [ args.text retain ];
    return args;
}

@implementation SipArguments

- (id)init:(SipArgs)args
{
   v = args;
   [ v.text retain ]; 
   return self;
}

- (SipArgs)values
{
   return v;
}

- (void)dealloc
{
   [ v.text release ]; 
}

@end

