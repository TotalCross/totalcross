#define Object NSObject*
#include "sipargs.h"
#include "mainview.h"

SipArgs SipArgsMake(int options, id control, bool secret, NSString *text)
{
    SipArgs args;
    args.options = options;
    args.control = control;
    return args;
}

@implementation SipArguments

- (id)init:(SipArgs)args
{
   v = args;
   return self;
}

- (SipArgs)values
{
   return v;
}

- (void)dealloc
{
}

@end

